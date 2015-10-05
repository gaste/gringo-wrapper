/*
 *  Copyright 2015 Philip Gasteiger
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.aau.grounder;

/**
 * Represents a grounder that takes a logic program and grounds it.
 * 
 * @author Philip Gasteiger
 *
 */
public interface Grounder {
	/**
	 * Ground the given logic program.
	 * 
	 * @param logicProgram
	 *            The logic program to ground in the gringo input format.
	 * @throws GroundingException
	 *             Thrown when the grounding failed because of errors.
	 * @return The grounded logic program in the gringo output format.
	 */
	public String ground(String logicProgram) throws GroundingException;
}
