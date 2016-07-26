package com.alex.upsolving;

import java.util.List;

public abstract class OnlineJudge {

	/**
	 * Get the list of unsolved problems
	 * 
	 * @return a list containing the unsolved problems
	 * @throws Exception 
	 */
	protected abstract List<Problem> getUnsolvedProblems() throws Exception;
}
