package uk.co.lbi.labyrinth;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import uk.co.lbi.labyrinth.Main.MazeNames;
import uk.co.lbi.labyrinth.domain.Route;
import uk.co.lbi.labyrinth.domain.Route.LocationTypes;
import uk.co.lbi.labyrinth.service.LabyrinthService;
import uk.co.lbi.labyrinth.service.LabyrinthServiceSpringImpl;

public class MutiThreadMain {

	private static Log log = LogFactory.getLog(MutiThreadMain.class);

	private static String RESULT = "labytinth.txt";

	private static LabyrinthService labyrinthService = new LabyrinthServiceSpringImpl(
			"http://labyrinth.lbi.co.uk");

	public static void main(String[] args) throws InterruptedException, ExecutionException,
			IOException {
		MazeNames maze = (MazeNames) JOptionPane.showInputDialog(null,
				"Select the maze you want to solve:", "Solve Maze", JOptionPane.QUESTION_MESSAGE,
				null, MazeNames.values(), MazeNames.easy);
		if (maze == null) {
			return;
		}
		ExecutorService threadPool = Executors.newCachedThreadPool();
		SolveMazeTask task = new SolveMazeTask(maze, "",
				"/Maze/Location/" + maze.name() + "/start", LocationTypes.Exit, null,
				labyrinthService, threadPool);
		Route route = threadPool.submit(task).get();
		String path = StringUtils.arrayToCommaDelimitedString(route.getPath()
				.toArray(new String[0]));
		String pills = StringUtils.arrayToCommaDelimitedString(route.getPowerPills().toArray(
				new String[0]));

		JTextArea textArea = new JTextArea(10, 50);
		JScrollPane pane = new JScrollPane(textArea);
		textArea.setOpaque(false);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		String content = "";
		String result = "";
		content += "\nMaze has been completed in " + route.getPath().size()
				+ " steps, using the following path:\n" + path + "\nand having eaten "
				+ route.numberOfPowerPills() + " power pills in the following locations: \n"
				+ pills;

		textArea.setText(content);
		log.info(content);
		result += content;
//		JOptionPane.showMessageDialog(null, pane, "Result", JOptionPane.INFORMATION_MESSAGE);

		task = new SolveMazeTask(maze, "", "/Maze/Location/" + maze.name() + "/"
				+ route.lastLocation(), LocationTypes.Start, null, labyrinthService, threadPool);
		route = threadPool.submit(task).get();
		threadPool.shutdown();

		path = StringUtils.arrayToCommaDelimitedString(route.getPath().toArray(new String[0]));
		String[] pillsArray = route.getPowerPills().toArray(new String[0]);
		CollectionUtils.reverseArray(pillsArray);
		pills = StringUtils.arrayToCommaDelimitedString(pillsArray);
		content = "";
		content += "\nMaze has been completed in " + route.getPath().size()
				+ " steps, using the following path:\n" + path + "\nand having eaten "
				+ route.numberOfPowerPills() + " power pills in the following locations: \n"
				+ pills;
		textArea.setText(content);
		log.info(content);
		result += content;
		FileUtils.writeStringToFile(new File(RESULT), result);
//		JOptionPane
//				.showMessageDialog(null, pane, "Reverse Result", JOptionPane.INFORMATION_MESSAGE);
	}

}
