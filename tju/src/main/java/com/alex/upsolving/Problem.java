package com.alex.upsolving;

/**
 * This class represents a problem from an Online Judge
 * 
 * @author Alexis Hernandez
 *
 */
public class Problem {
	// the id of the problem
	private final String id;
	
	// the name of the problem
	private final String name;
	
	/**
	 * Creates a problem with the given arguments
	 * 
	 * @param id the id of the problem
	 * @param name the name of the problem
	 */
	public Problem(String id, String name)	{
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String toString()	{
		return	id + " - " + name;
	}
}


