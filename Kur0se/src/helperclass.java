import java.time.DayOfWeek;

import DataStructures.TimeBlock;

public class helperclass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 7; ++i) {
			for (int j = 0; j < 24; ++j) {
				for (int k = 0; k < 60; k += 30) {
					System.out.println("timeList.add(new TimeBlock(DayOfWeek.," +j +"," + k+"));");
	
				}
			}
		}
	}

}
