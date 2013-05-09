package uk.co.lbi.labyrinth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import uk.co.lbi.labyrinth.Main.MazeNames;
import uk.co.lbi.labyrinth.domain.Route;
import uk.co.lbi.labyrinth.domain.Route.LocationTypes;
import uk.co.lbi.labyrinth.jaxb.Location;
import uk.co.lbi.labyrinth.service.LabyrinthService;

public class SolveMazeTask implements Callable<Route> {

	private MazeNames maze;
	private String source;
	private String destination;
	private LocationTypes endCriteria;
	private HashSet<String> checkedRoutes;
	private LabyrinthService labyrinthService;
	private ExecutorService threadPool;

	public SolveMazeTask(MazeNames maze, String source, String destination,
			LocationTypes endCriteria, HashSet<String> checkedRoutes,
			LabyrinthService labyrinthService, ExecutorService threadPool) {
		this.maze = maze;
		this.source = source;
		this.destination = destination;
		this.endCriteria = endCriteria;
		this.checkedRoutes = checkedRoutes == null ? new HashSet<String>() : checkedRoutes;
		this.labyrinthService = labyrinthService;
		this.threadPool = threadPool;
	}

	@SuppressWarnings("unchecked")
	public Route call() throws Exception {
		Route route = new Route();
		List<String> exits = new ArrayList<String>();
		Location locationResult = labyrinthService.checkPath(destination);
		checkedRoutes.add("/Maze/Location/" + maze.name() + "/" + locationResult.getLocationId());
		route.next(locationResult);
		exits.addAll(locationResult.getExits());
		if (endCriteria.equals(route.getResult())) {
			return route;
		} else if (LocationTypes.PowerPill.equals(route.getResult())) {
			route.addPowerPills(locationResult.getLocationId());
		}
		List<SolveMazeTask> tasks = new ArrayList<SolveMazeTask>();
		for (String exit : exits) {
			if (!checkedRoutes.contains(exit) && !exit.equals(source)) {
				tasks.add(new SolveMazeTask(maze, destination, exit, endCriteria,
						(HashSet<String>) checkedRoutes.clone(), labyrinthService, threadPool));
				System.gc();
			}
		}
		List<Future<Route>> results = threadPool.invokeAll(tasks);
		Route shortestRoute = processResults(results);
		if (shortestRoute != null) {
			route.continueTo(shortestRoute);
		}
		return route;
	}

	private Route processResults(List<Future<Route>> results) {
		Route shortestRoute = null;
		for (Future<Route> result : results) {
			Route newRoute;
			try {
				newRoute = result.get();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (!endCriteria.equals(newRoute.getResult())) {
				continue;
			}
			boolean noShortestRoot = shortestRoute == null;
			boolean newRouteShorter = !noShortestRoot && newRoute.length() < shortestRoute.length();
			boolean newRouteHasPowerPills = newRoute.numberOfPowerPills() > 0;
			boolean shortestRouteHasPowerPills = !noShortestRoot
					&& shortestRoute.numberOfPowerPills() > 0;
			if (noShortestRoot || (newRouteHasPowerPills && !shortestRouteHasPowerPills)
					|| (newRouteHasPowerPills && shortestRouteHasPowerPills && newRouteShorter)
					|| (!newRouteHasPowerPills && !shortestRouteHasPowerPills && newRouteShorter)) {
				shortestRoute = newRoute;
			}
			if (newRouteHasPowerPills) {
				System.err.println(noShortestRoot + "\t" + newRouteHasPowerPills + "\t"
						+ shortestRouteHasPowerPills + "\t" + newRouteShorter);
			}
		}
		return shortestRoute;
	}

}
