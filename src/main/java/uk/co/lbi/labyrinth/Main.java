package uk.co.lbi.labyrinth;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import uk.co.lbi.labyrinth.domain.Route;
import uk.co.lbi.labyrinth.domain.Route.LocationTypes;
import uk.co.lbi.labyrinth.jaxb.Location;
import uk.co.lbi.labyrinth.service.LabyrinthService;
import uk.co.lbi.labyrinth.service.LabyrinthServiceSpringImpl;

public class Main {

	public static enum MazeNames {
		easy, pacman, glasgow;
	}

	private static String RESULT = "labytinth.txt";

	private LabyrinthService labyrinthService = new LabyrinthServiceSpringImpl(
			"http://labyrinth.lbi.co.uk");
	private static int counter = 0;

	public Route solveLabyrinth(MazeNames maze, String source, String destination,
			LocationTypes endCriteria, HashSet<Integer> checkedNodes) {
		counter++;
		if (checkedNodes == null) {
			checkedNodes = new HashSet<Integer>();
		}
		Route route = new Route();
		Set<String> exits = new HashSet<String>();
		Location locationResult = labyrinthService.checkPath(destination);
		String node = "/Maze/Location/" + maze.name() + "/" + locationResult.getLocationId();
		checkedNodes.add(node.hashCode());
		route.next(locationResult);
		exits.addAll(locationResult.getExits());
		if (endCriteria.equals(route.getResult())) {
			return route;
		} else if (LocationTypes.PowerPill.equals(route.getResult())) {
			route.addPowerPills(locationResult.getLocationId());
		}
		Route shortestRoute = null;
		for (String exit : exits) {
			if (!checkedNodes.contains(exit.hashCode()) && !exit.equals(source)) {
				@SuppressWarnings("unchecked")
				Route newRoute = solveLabyrinth(maze, destination, exit, endCriteria,
						(HashSet<Integer>) checkedNodes.clone());
				if (!endCriteria.equals(newRoute.getResult())) {
					continue;
				}
				boolean noShortestRoot = shortestRoute == null;
				boolean newRouteShorter = !noShortestRoot
						&& newRoute.length() < shortestRoute.length();
				boolean newRouteHasPowerPills = newRoute.numberOfPowerPills() > 0;
				boolean shortestRouteHasPowerPills = !noShortestRoot
						&& shortestRoute.numberOfPowerPills() > 0;
				if (noShortestRoot
						|| (newRouteHasPowerPills && !shortestRouteHasPowerPills)
						|| (newRouteHasPowerPills && shortestRouteHasPowerPills && newRouteShorter)
						|| (!newRouteHasPowerPills && !shortestRouteHasPowerPills && newRouteShorter)) {
					shortestRoute = newRoute;
				}
			}
		}
		if (shortestRoute != null) {
			route.continueTo(shortestRoute);
		}
		return route;
	}

	private void initialize() {
		counter = 0;
	}

	public static void main(String[] args) throws IOException {
		MazeNames maze = (MazeNames) JOptionPane.showInputDialog(null,
				"Select the maze you want to solve:", "Solve Maze", JOptionPane.QUESTION_MESSAGE,
				null, MazeNames.values(), MazeNames.easy);
		if (maze == null) {
			return;
		}
		Main main = new Main();
		Route route = main.solveLabyrinth(maze, "", "/Maze/Location/" + maze.name() + "/start",
				LocationTypes.Exit, null);
		String path = StringUtils.arrayToCommaDelimitedString(route.getPath()
				.toArray(new String[0]));
		String pills = StringUtils.arrayToCommaDelimitedString(route.getPowerPills().toArray(
				new String[0]));

		String result = "";
		String content = String.valueOf(counter);
		content += "\nMaze has been completed in " + route.getPath().size()
				+ " steps, using the following path:\n" + path + "\nand having eaten "
				+ route.numberOfPowerPills() + " power pills in the following locations: \n"
				+ pills;

		result += content;

		main.initialize();
		route = main.solveLabyrinth(maze, "",
				"/Maze/Location/" + maze.name() + "/" + route.lastLocation(), LocationTypes.Start,
				null);

		path = StringUtils.arrayToCommaDelimitedString(route.getPath().toArray(new String[0]));
		String[] pillsArray = route.getPowerPills().toArray(new String[0]);
		CollectionUtils.reverseArray(pillsArray);
		pills = StringUtils.arrayToCommaDelimitedString(pillsArray);
		content = String.valueOf(counter);
		content += "\nMaze has been completed in " + route.getPath().size()
				+ " steps, using the following path:\n" + path + "\nand having eaten "
				+ route.numberOfPowerPills() + " power pills in the following locations: \n"
				+ pills;
		result += content;
		FileUtils.writeStringToFile(new File(RESULT), result);
	}

}
