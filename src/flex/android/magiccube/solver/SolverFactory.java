package flex.android.magiccube.solver;

public class SolverFactory {
	public static MagicCubeSolver CreateSolver(String SolverName)
	{
		if( SolverName == "Jaap")
		{
			return new JaapSolver();
		}
		
		return null;
	}
}
