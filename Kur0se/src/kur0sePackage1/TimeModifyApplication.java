package kur0sePackage1;

import java.io.IOException;

import DataStructures.FileBlock;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class TimeModifyApplication extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;

	@FXML
	private TableView<FileBlock> FileTable;
	@FXML
	private TableColumn<FileBlock, String> fileCol;
	@FXML
	private TableColumn<FileBlock, String> pathCol;

	private ObservableList<FileBlock> fileList = FXCollections.observableArrayList(new FileBlock("asdf", "asdf"));

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Television Network Emualtor");

		initRootLayout();

		// showPersonOverview();
	}

	@FXML
	private void handleButtonAction(ActionEvent event) {
		System.out.println("You clicked me!");
	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(TimeModifyApplication.class.getResource("TimeModifyView.fxml"));

			TimeModifyController controller = new TimeModifyController();
			loader.setController(controller);
			primaryStage.getIcons().add(
					   new Image("Resources/tvIcon.png")); 
			controller.setStageAndSetupListeners(this.primaryStage); // or what
																		// you
																		// want
																		// to
			// do
			
			rootLayout = (AnchorPane) loader.load();

			// So apparently it automaticall intiializes when the thing is created I guess. 
			// controller.initialize();
			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(e -> System.exit(0));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows the person overview inside the root layout.
	 */
	public void showPersonOverview() {
		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(TimeModifyApplication.class.getResource("layouts/PersonOverview.fxml"));
			AnchorPane personOverview = (AnchorPane) loader.load();

			// Set person overview into the center of root layout.
			// rootLayout.setCenter(personOverview);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the main stage.
	 * 
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}