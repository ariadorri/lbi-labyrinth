package uk.co.lbi.labyrinth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import uk.co.lbi.labyrinth.domain.Route;
import uk.co.lbi.labyrinth.domain.Route.LocationTypes;
import uk.co.lbi.labyrinth.jaxb.Location;
import uk.co.lbi.labyrinth.service.LabyrinthService;
import uk.co.lbi.labyrinth.service.LabyrinthServiceSpringImpl;

public class Main {

	private static enum MazeNames {
		easy, pacman, glasgow;
	}

	private LabyrinthService labyrinthService = new LabyrinthServiceSpringImpl(
			"http://labyrinth.lbi.co.uk");
	private static int counter = 0;

	public Route solveLabyrinth(MazeNames maze, String source, String destination,
			LocationTypes endCriteria, HashSet<String> checkedRoutes) {
		counter++;
		boolean start = checkedRoutes == null;
		Route route = new Route();
		List<String> exits = new ArrayList<String>();
		Location locationResult = labyrinthService.checkPath(destination, start);
		if (!start) {
			checkedRoutes.add(destination);
		}
		if (locationResult != null) {
			if (start) {
				checkedRoutes = new HashSet<String>();
				checkedRoutes.add("/Maze/Location/" + maze.name() + "/"
						+ locationResult.getLocationId());
			}
			route.next(locationResult);
			exits.addAll(locationResult.getExits());
			if (endCriteria.equals(route.getResult())) {
				return route;
			} else if (LocationTypes.PowerPill.equals(route.getResult())) {
				route.addPowerPills(locationResult.getLocationId());
			}
			Route shortestRoute = null;
			for (String exit : exits) {
				if (!checkedRoutes.contains(exit) && !exit.equals(source)) {
					@SuppressWarnings("unchecked")
					Route newRoute = solveLabyrinth(maze, destination, exit, endCriteria,
							(HashSet<String>) checkedRoutes.clone());
					if (newRoute != null) {
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
			}
			if (shortestRoute != null) {
				route.continueTo(shortestRoute);
				return route;
			}
		}
		return null;
	}

	private void initialize() {
		counter = 0;
	}

	public static void main(String[] args) {
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

		JTextArea textArea = new JTextArea(10, 50);
		JScrollPane pane = new JScrollPane(textArea);
		textArea.setOpaque(false);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		String content = String.valueOf(counter);
		content += "\nMaze has been completed in " + route.getPath().size()
				+ " steps, using the following path:\n" + path + "\nand having eaten "
				+ route.numberOfPowerPills() + " power pills in the following locations: \n"
				+ pills;

		textArea.setText(content);
		JOptionPane.showMessageDialog(null, pane, "Result", JOptionPane.INFORMATION_MESSAGE);

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
		textArea.setText(content);
		JOptionPane.showMessageDialog(null, pane, "Reverse Result", JOptionPane.INFORMATION_MESSAGE);
	}

}
