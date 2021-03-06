package kur0sePackage1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import DataStructures.FileBlock;
import DataStructures.TimeBlock;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class TimeModifyController {

	@FXML
	private Button BrowseButton;
	@FXML
	private TableView<TimeBlock> TimeBlocks;
	@FXML
	private TextField FolderPath;
	@FXML
	private Button RandomBox;
	@FXML
	private TableView<FileBlock> FileTable;
	@FXML
	private TableColumn<FileBlock, String> fileCol;
	@FXML
	private TableColumn<FileBlock, String> pathCol;
	@FXML
	private TableColumn<TimeBlock, String> TimeBlockName;
	@FXML
	private Label day;
	@FXML
	private Label time;
	@FXML
	private Label currentTime;
	@FXML
	private BorderPane videoPane;
	@FXML
	private Button saveTimeBlocks;
	@FXML
	private Button fullScreenButton;
	@FXML
	private Button startButton;
	// Slider for Mediaplayer
	private Slider timeSlider;
	private Slider volumeSlider;
	// Mediaplayer box
	private HBox mediaBar;
	final Button playButton = new Button(">");
	private final boolean repeat = false;
	private boolean stopRequested = false;
	private boolean atEndOfMedia = false;
	private Duration duration;
	private Timer timer;
	private Label playTime;
	private MediaView mediaView;
	private MediaPlayer mediaPlayer;
	private TimeBlock currentTimeBlockView;
	private ObservableList<FileBlock> fileList = FXCollections.observableArrayList();
	private ObservableList<TimeBlock> timeList = FXCollections.observableArrayList();

	private TimeBlock currentPlayingTimeblock;

	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	Stage stage;

	public void initialize() {
		// TimeTable init
		initializeTimeList();
		initMediaView();
		initFileTable();
		initFileList();
		initTimeBlocks();
		initCurrentPlayingTimeBlock();

	}

	private void initCurrentPlayingTimeBlock() {
		// updateTimeBlockHashmap();
		LocalTime now = LocalTime.now();
		int theMinute = now.getMinute();
		int minuteRoundedDown = (theMinute / 30 == 0 ? 0 : 30);

		LocalDate date = LocalDate.now();
		DayOfWeek day = date.getDayOfWeek();

		// currentPlayingTimeblock = timeBlockMap.get(day + " | " +
		// now.getHour() + ":" + minuteRoundedDown);
		currentPlayingTimeblock = timeList.get(0);
		this.stage.setTitle(day + " | " + now.getHour() + ":" + minuteRoundedDown);

	}

	private void initTimeBlocks() {
		TimeBlockName.setCellValueFactory(new PropertyValueFactory<>("timeBlockName"));
		TimeBlocks.setItems(timeList);
		TimeBlocks.sortPolicyProperty().set(new Callback<TableView<TimeBlock>, Boolean>() {
			@Override
			public Boolean call(TableView<TimeBlock> param) {
				Comparator<TimeBlock> comparator = new Comparator<TimeBlock>() {
					@Override
					public int compare(TimeBlock r1, TimeBlock r2) {
						return 0;
					}
				};
				FXCollections.sort(TimeBlocks.getItems(), comparator);
				return true;
			}
		});

		TimeBlocks.setRowFactory(tv -> {
			TableRow<TimeBlock> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					TimeBlock rowData = row.getItem();
					System.out.println(rowData.getDay().toString());
					this.day.setText(rowData.getDay().toString());
					this.time.setText(rowData.getTimeInString());
					this.currentTimeBlockView = rowData;
					this.fileList.clear();
					this.fileList.addAll(this.currentTimeBlockView.getFileList());
					this.FolderPath.setText(rowData.getFilePath());
				}
			});
			return row;
		});
	}

	private void initFileList() {
		TimeBlock rowData = timeList.get(0);
		System.out.println(rowData.getDay().toString());
		this.day.setText(rowData.getDay().toString());
		this.time.setText(rowData.getTimeInString());
		this.currentTimeBlockView = rowData;
		this.fileList.clear();
		this.fileList.addAll(this.currentTimeBlockView.getFileList());
	}

	private void initTimerAndMediaBar() throws Exception {
		// find
		LocalTime now = LocalTime.now();
		int theMinute = now.getMinute();
		int minuteRoundedDown = (theMinute / 30 == 0 ? 0 : 30);
		timer = new Timer();
		LocalDate date = LocalDate.now();
		DayOfWeek day = date.getDayOfWeek();
		long timePassed = (30 * 60 * 1000) - ((theMinute * 60 * 1000) - (minuteRoundedDown * 60 * 1000));
		// Calculates time passed from the floor of the half hour
		// This is where we set the current playing time block
		// currentPlayingTimeblock = timeBlockMap.get(day + " | " +
		// now.getHour() + ":" + minuteRoundedDown);

		/*
		 * for (int i = 0; i < timeList.size(); ++i) { if
		 * (TimeBlocks.getItems().get(i).getHour() == now.getHour() &&
		 * TimeBlocks.getItems().get(i).getMinute() == minuteRoundedDown &&
		 * TimeBlocks.getItems().get(i).getDay() == day) {
		 * currentPlayingTimeblock = TimeBlocks.getItems().get(i); break; } }
		 */

		// above code should be done in setcurrentplaytime
		setCurrentPlayTime(now.getHour(), minuteRoundedDown, day);
		if (currentPlayingTimeblock.getFileList().get(currentPlayingTimeblock.getVideoPlace()).getFilePath() == "") {
			throw new Exception("No current playlist");
		}
		initMediaPlayer();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {

					public void run() {
						// TODO: every thirty minutes
						nextTimeBlock();
						System.out.println(currentPlayingTimeblock.getTimeBlockName());
						// updateMediaPlayer();
						System.out.println(currentPlayingTimeblock.getFilePath());

					}
				});
			}
		}, timePassed, 1800000);// theminute
		videoPane.setBottom(addToolBar());
		// -minuteRoundedDown
		// =
		// difference
		// between
		// rounding
		// down
		// halfway.
		// so 30 minutes - (difference between current and rounded down half
		// hour) = amount to reach next half hour, therefore that is the delay

	}

	private void setCurrentPlayTime(int hour, int minute, DayOfWeek day) {
		for (int i = 0; i < timeList.size(); ++i) {
			if (TimeBlocks.getItems().get(i).getHour() == hour && TimeBlocks.getItems().get(i).getMinute() == minute
					&& TimeBlocks.getItems().get(i).getDay() == day) {
				currentPlayingTimeblock = TimeBlocks.getItems().get(i);
				break;
			}
		}
	}

	private void updateTimeBlockView() {
		this.currentTimeBlockView.setFileList(FXCollections.observableArrayList(fileList));
		for (int i = 0; i < timeList.size(); ++i) {
			if (timeList.get(i).getHour() == currentTimeBlockView.getHour()
					&& timeList.get(i).getMinute() == currentTimeBlockView.getMinute()
					&& timeList.get(i).getDay() == currentTimeBlockView.getDay()) {
				timeList.set(i, currentTimeBlockView);
			}
		}
	}

	// This doesn't actually update the current playing timeblock... bad design
	private void initMediaPlayer() {
		// Makes it loop around
		if (currentPlayingTimeblock.getFileList().size() < currentPlayingTimeblock.getVideoPlace() + 1) {
			System.out.println("ran in looper");
		}
		if (currentPlayingTimeblock.getFileList().size() == 0) {
			System.out.println("Empty");
			timer.cancel();
			startButton.setDisable(false);
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Empty Playlist for Current Time Block");
			alert.setHeaderText("Empty Playlist");
			alert.setContentText(
					"Please update the current time with a playlist, otherwise this application will not work correctly. The timer has been stopped, please press start to restart the timer");
			alert.showAndWait();
		} else {
			try {
				System.out.println("Ran in here?");
				Media media = new Media(currentPlayingTimeblock.getFileList()
						.get(currentPlayingTimeblock.getVideoPlace()).getFilePath());
				currentPlayingTimeblock.setVideoPlace(currentPlayingTimeblock.getVideoPlace() + 1);
				mediaPlayer = new MediaPlayer(media);
				mediaView.setMediaPlayer(mediaPlayer);
				mediaPlayer.setAutoPlay(true);
				videoPane.setBottom(addToolBar());
				duration = mediaPlayer.getMedia().getDuration();
				updateValues();
				mediaPlayer.setOnEndOfMedia(new Runnable() {
					@Override
					public void run() {
						initMediaPlayer();
						System.out.println("end of media");

					}
				});
			} catch (Exception e) {
				timer.cancel();
				startButton.setDisable(false);
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Next video in playlist is in unplayable format");
				alert.setHeaderText("Please modify playlist and make next item playable");
				alert.setContentText(
						"Please update the current time with a playable playlist, otherwise this application will not work correctly. The timer has been stopped, please press start to restart the timer");
				alert.showAndWait();
			}
		}

	}

	@FXML
	private void fullscreenClick(ActionEvent event) throws IOException {
		this.stage.setFullScreen(!this.stage.isFullScreen());
	}

	@FXML
	private void saveTimeBlocks(ActionEvent event) throws IOException {
		try {
			// write object to file
			FileOutputStream fos = new FileOutputStream("Objectsavefile.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			// TODO: figure this out... It won't work because the timeblocks
			// have observable lists which don't work with serialization of
			// object
			oos.writeObject(new ArrayList<TimeBlock>(timeList));
			oos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void nextTimeBlock() {
		int nextHour = currentPlayingTimeblock.getHour();
		int nextMinute = currentPlayingTimeblock.getMinute();
		DayOfWeek nextDay = currentPlayingTimeblock.getDay();
		if (nextHour == 23 && nextMinute == 30) {
			// handles next day
			nextDay = currentPlayingTimeblock.getDay().plus(1);
			nextHour = 0;
			nextMinute = 0;
		} else {
			// handles 30 minute to next hour, and handles o clock + 30 minutes
			if (nextMinute == 30) { // handles 30 minute
				nextHour++;
				nextMinute -= 30;
			} else { // handles 0 minute
				nextMinute += 30;
			}
		}
		String timeInString = nextHour + ":" + nextMinute;
		// currentPlayingTimeblock ;= timeBlockMap.get(nextDay.toString() + " |
		// " + timeInString)
		this.stage.setTitle(nextDay + " | " + nextHour + ":" + nextMinute);
		// TODO:
		setCurrentPlayTime(nextHour, nextMinute, nextDay);
	}

	private void initFileTable() {
		// FileTable Init
		fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		pathCol.setCellValueFactory(new PropertyValueFactory<>("filePath"));
		FileTable.setItems(fileList);
		FileTable.setEditable(true);

		// on drag
		FileTable.setRowFactory(tv -> {
			TableRow<FileBlock> row = new TableRow<>();
			row.setOnDragDetected(event -> {
				if (!row.isEmpty()) {
					Integer index = row.getIndex();
					Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					ClipboardContent cc = new ClipboardContent();
					cc.put(SERIALIZED_MIME_TYPE, index);
					db.setContent(cc);
					event.consume();
				}
			});

			// dragging
			row.setOnDragOver(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						event.consume();
					}
				}
			});

			// droped
			row.setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
					FileBlock draggedPerson = FileTable.getItems().remove(draggedIndex);

					int dropIndex;

					if (row.isEmpty()) {
						dropIndex = FileTable.getItems().size();
					} else {
						dropIndex = row.getIndex();
					}

					FileTable.getItems().add(dropIndex, draggedPerson);
					event.setDropCompleted(true);
					FileTable.getSelectionModel().select(dropIndex);

					updateTimeBlockView();

					event.consume();

				}
			});

			return row;
		}); // FileTable Init
		fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		pathCol.setCellValueFactory(new PropertyValueFactory<>("filePath"));
		FileTable.setItems(fileList);
		FileTable.setEditable(true);

		FileTable.setRowFactory(tv -> {
			TableRow<FileBlock> row = new TableRow<>();
			row.setOnDragDetected(event -> {
				if (!row.isEmpty()) {
					Integer index = row.getIndex();
					Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					ClipboardContent cc = new ClipboardContent();
					cc.put(SERIALIZED_MIME_TYPE, index);
					db.setContent(cc);
					event.consume();
				}
			});

			row.setOnDragOver(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						event.consume();
					}
				}
			});

			row.setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
					FileBlock draggedPerson = FileTable.getItems().remove(draggedIndex);

					int dropIndex;

					if (row.isEmpty()) {
						dropIndex = FileTable.getItems().size();
					} else {
						dropIndex = row.getIndex();
					}

					FileTable.getItems().add(dropIndex, draggedPerson);
					event.setDropCompleted(true);
					FileTable.getSelectionModel().select(dropIndex);
					updateTimeBlockView();
					event.consume();

				}
			});

			return row;
		});
	}

	// Horrible name, handle browse button
	@FXML
	private void handleButtonAction(ActionEvent event) {

		DirectoryChooser chooser = new DirectoryChooser();
		File defaultDirectory = new File("c:/");
		chooser.setInitialDirectory(defaultDirectory);
		File selectedDirectory = chooser.showDialog(stage);
		FolderPath.setText(selectedDirectory.getAbsolutePath());
		FileTable.getItems().clear();
		this.currentTimeBlockView.setFilePath(selectedDirectory.getAbsolutePath());
		for (final File fileEntry : selectedDirectory.listFiles()) {
			FileBlock block = new FileBlock(fileEntry.getName(), fileEntry.toURI().toString());
			System.out.println(block.getFileName());
			fileList.add(block);
		}
		updateTimeBlockView();

	}

	@FXML
	private void randomizeList() {
		// TODO: just randomize list, ez
		long seed = System.nanoTime();
		Collections.shuffle(fileList, new Random(seed));
		updateTimeBlockView();
	}

	public void initMediaView() {
		mediaView = new MediaView();
		mediaView.setMediaPlayer(mediaPlayer);
		videoPane.setCenter(mediaView);
		videoPane.setMinHeight(200);
		videoPane.setMinWidth(500);
		videoPane.setStyle("-fx-background-color: Black");
		DropShadow dropshadow = new DropShadow();
		dropshadow.setOffsetY(5.0);
		dropshadow.setOffsetX(5.0);
		dropshadow.setColor(Color.WHITE);
		mediaView.setEffect(dropshadow);
		DoubleProperty mvw = mediaView.fitWidthProperty();
		DoubleProperty mvh = mediaView.fitHeightProperty();
		mvw.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
		mvh.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
		mediaView.setPreserveRatio(true);

	}

	@FXML
	private void start() {

		try {
			initTimerAndMediaBar();
			startButton.setDisable(true);
		} catch (Exception e) {
			System.out.println(e);
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Empty Playlist for Current Time");
			alert.setHeaderText("Empty Playlist");
			alert.setContentText(
					"Please update the current time with a playlist, otherwise this application will not work correctly.");
			alert.showAndWait();
		}

	}

	protected void updateValues() {
		if (playTime != null && timeSlider != null && volumeSlider != null && duration != null) {
			Platform.runLater(new Runnable() {
				public void run() {
					Duration currentTime = mediaView.getMediaPlayer().getCurrentTime();
					playTime.setText(formatTime(currentTime, duration));
					timeSlider.setDisable(duration.isUnknown());
					if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO)
							&& !timeSlider.isValueChanging()) {
						timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
					}
					if (!volumeSlider.isValueChanging()) {
						volumeSlider.setValue((int) Math.round(mediaView.getMediaPlayer().getVolume() * 100));
					}
				}
			});
		}
	}

	private HBox addToolBar() {

		mediaBar = new HBox();
		mediaBar.setAlignment(Pos.CENTER);
		mediaBar.setPadding(new Insets(5, 10, 5, 10));
		BorderPane.setAlignment(mediaBar, Pos.CENTER);
		mediaBar.getChildren().add(playButton);
		timeSlider = new Slider();
		HBox.setHgrow(timeSlider, Priority.ALWAYS);
		timeSlider.setMinWidth(50);
		timeSlider.setMaxWidth(Double.MAX_VALUE / 2);
		mediaBar.getChildren().add(timeSlider);
		playTime = new Label();
		playTime.setPrefWidth(130);
		playTime.setMinWidth(50);
		mediaBar.getChildren().add(playTime);
		// Add the volume label
		Label volumeLabel = new Label("Vol: ");
		mediaBar.getChildren().add(volumeLabel);
		// Add Volume slider
		volumeSlider = new Slider();
		volumeSlider.setPrefWidth(70);
		volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
		volumeSlider.setMinWidth(30);

		mediaBar.getChildren().add(volumeSlider);

		playButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				Status status = mediaView.getMediaPlayer().getStatus();

				if (status == Status.UNKNOWN || status == Status.HALTED) {
					// don't do anything in these states
					return;
				}

				if (status == Status.PAUSED || status == Status.READY || status == Status.STOPPED) {
					// rewind the movie if we're sitting at the end
					if (atEndOfMedia) {
						mediaView.getMediaPlayer().seek(mediaPlayer.getStartTime());
						atEndOfMedia = false;
					}

					mediaView.getMediaPlayer().play();
				} else {

					mediaView.getMediaPlayer().pause();
				}
			}
		});

		mediaView.getMediaPlayer().currentTimeProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				updateValues();
			}
		});

		mediaView.getMediaPlayer().setOnPlaying(new Runnable() {
			public void run() {
				if (stopRequested) {
					mediaView.getMediaPlayer().pause();
					stopRequested = false;
				} else {
					playButton.setText("||");
				}
			}
		});

		mediaView.getMediaPlayer().setOnPaused(new Runnable() {
			public void run() {
				System.out.println("onPaused");
				playButton.setText(">");
			}
		});

		mediaView.getMediaPlayer().setOnReady(new Runnable() {
			public void run() {
				duration = mediaView.getMediaPlayer().getMedia().getDuration();
				updateValues();
			}
		});

		timeSlider.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				if (timeSlider.isValueChanging()) {
					// multiply duration by percentage calculated by slider
					// position
					if (duration != null) {

						mediaView.getMediaPlayer().seek(duration.multiply(timeSlider.getValue() / 100.0));
					}
					updateValues();
				}
			}
		});
		volumeSlider.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				if (volumeSlider.isValueChanging()) {

					mediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100.0);
				}
			}
		});

		return mediaBar;
	}

	private static String formatTime(Duration elapsed, Duration duration) {
		int intElapsed = (int) Math.floor(elapsed.toSeconds());
		int elapsedHours = intElapsed / (60 * 60);
		if (elapsedHours > 0) {
			intElapsed -= elapsedHours * 60 * 60;
		}
		int elapsedMinutes = intElapsed / 60;
		int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

		if (duration.greaterThan(Duration.ZERO)) {
			int intDuration = (int) Math.floor(duration.toSeconds());
			int durationHours = intDuration / (60 * 60);
			if (durationHours > 0) {
				intDuration -= durationHours * 60 * 60;
			}
			int durationMinutes = intDuration / 60;
			int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
			if (durationHours > 0) {
				return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds,
						durationHours, durationMinutes, durationSeconds);
			} else {
				return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes,
						durationSeconds);
			}
		} else {
			if (elapsedHours > 0) {
				return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
			} else {
				return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
			}
		}
	}

	public void setStageAndSetupListeners(Stage primaryStage) {
		stage = primaryStage;
	}

	public void initializeTimeList() {
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 0, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 0, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 1, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 1, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 2, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 2, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 3, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 3, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 4, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 4, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 5, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 5, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 6, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 6, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 7, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 7, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 8, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 8, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 9, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 9, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 10, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 10, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 11, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 11, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 12, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 12, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 13, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 13, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 14, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 14, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 15, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 15, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 16, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 16, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 17, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 17, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 18, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 18, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 19, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 19, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 20, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 20, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 21, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 21, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 22, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 22, 30));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 23, 0));
		timeList.add(new TimeBlock(DayOfWeek.MONDAY, 23, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 0, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 0, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 1, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 1, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 2, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 2, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 3, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 3, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 4, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 4, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 5, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 5, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 6, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 6, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 7, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 7, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 8, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 8, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 9, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 9, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 10, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 10, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 11, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 11, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 12, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 12, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 13, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 13, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 14, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 14, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 15, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 15, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 16, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 16, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 17, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 17, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 18, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 18, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 19, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 19, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 20, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 20, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 21, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 21, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 22, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 22, 30));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 23, 0));
		timeList.add(new TimeBlock(DayOfWeek.TUESDAY, 23, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 0, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 0, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 1, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 1, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 2, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 2, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 3, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 3, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 4, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 4, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 5, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 5, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 6, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 6, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 7, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 7, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 8, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 8, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 9, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 9, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 10, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 10, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 11, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 11, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 12, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 12, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 13, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 13, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 14, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 14, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 15, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 15, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 16, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 16, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 17, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 17, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 18, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 18, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 19, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 19, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 20, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 20, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 21, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 21, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 22, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 22, 30));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 23, 0));
		timeList.add(new TimeBlock(DayOfWeek.WEDNESDAY, 23, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 0, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 0, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 1, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 1, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 2, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 2, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 3, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 3, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 4, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 4, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 5, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 5, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 6, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 6, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 7, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 7, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 8, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 8, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 9, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 9, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 10, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 10, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 11, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 11, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 12, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 12, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 13, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 13, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 14, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 14, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 15, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 15, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 16, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 16, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 17, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 17, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 18, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 18, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 19, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 19, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 20, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 20, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 21, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 21, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 22, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 22, 30));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 23, 0));
		timeList.add(new TimeBlock(DayOfWeek.THURSDAY, 23, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 0, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 0, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 1, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 1, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 2, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 2, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 3, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 3, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 4, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 4, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 5, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 5, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 6, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 6, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 7, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 7, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 8, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 8, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 9, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 9, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 10, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 10, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 11, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 11, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 12, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 12, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 13, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 13, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 14, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 14, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 15, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 15, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 16, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 16, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 17, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 17, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 18, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 18, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 19, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 19, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 20, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 20, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 21, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 21, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 22, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 22, 30));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 23, 0));
		timeList.add(new TimeBlock(DayOfWeek.FRIDAY, 23, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 0, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 0, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 1, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 1, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 2, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 2, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 3, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 3, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 4, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 4, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 5, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 5, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 6, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 6, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 7, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 7, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 8, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 8, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 9, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 9, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 10, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 10, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 11, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 11, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 12, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 12, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 13, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 13, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 14, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 14, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 15, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 15, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 16, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 16, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 17, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 17, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 18, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 18, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 19, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 19, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 20, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 20, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 21, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 21, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 22, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 22, 30));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 23, 0));
		timeList.add(new TimeBlock(DayOfWeek.SATURDAY, 23, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 0, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 0, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 1, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 1, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 2, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 2, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 3, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 3, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 4, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 4, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 5, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 5, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 6, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 6, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 7, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 7, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 8, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 8, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 9, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 9, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 10, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 10, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 11, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 11, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 12, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 12, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 13, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 13, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 14, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 14, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 15, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 15, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 16, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 16, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 17, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 17, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 18, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 18, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 19, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 19, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 20, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 20, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 21, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 21, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 22, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 22, 30));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 23, 0));
		timeList.add(new TimeBlock(DayOfWeek.SUNDAY, 23, 30));
	}
}
