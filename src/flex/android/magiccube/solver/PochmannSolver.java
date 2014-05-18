/*
 * Copyright 2011-2014 Zhaotian Wang <zhaotianzju@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flex.android.magiccube.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
public class PochmannSolver extends MagicCubeSolver{


	//----------------------------------------------------------------------
	//----------------------------------------------------------------------

	private int applicableMoves[] = { 0, 262143, 259263, 74943, 74898 };

	// TODO: Encode as strings, e.g. for U use "ABCDABCD"

	private int affectedCubies[][] = {
	  {  0,  1,  2,  3,  0,  1,  2,  3 },   // U
	  {  4,  7,  6,  5,  4,  5,  6,  7 },   // D
	  {  0,  9,  4,  8,  0,  3,  5,  4 },   // F
	  {  2, 10,  6, 11,  2,  1,  7,  6 },   // B
	  {  3, 11,  7,  9,  3,  2,  6,  5 },   // L
	  {  1,  8,  5, 10,  1,  0,  4,  7 },   // R
	};

	private List<Integer> applyMove ( int move, List<Integer> state ) 
	{
	  int turns = move % 3 + 1;
	  int face = move / 3;
	  while( (turns--)!=0 ){
		  List<Integer> oldState = state;
	    for( int i=0; i<8; i++ ){
	      int isCorner = (i > 3)?1:0;
	      int target = affectedCubies[face][i] + isCorner*12;
	      int killer = affectedCubies[face][(i&3)==3 ? i-3 : i+1] + isCorner*12;;
	      int orientationDelta = (i<4) ? ((face>1 && face<4)?1:0) : (face<2) ? 0 : 2 - (i&1);
	      state.set( target, oldState.get(killer));
	      //state[target+20] = (oldState[killer+20] + orientationDelta) % (2 + isCorner);
	      state.set(target+20, oldState.get(killer+20) + orientationDelta);
	      if( turns==0 )
	    	  state.set(target+20, state.get(target+20)%(2 + isCorner));
	    }
	  }
	  return state;
	}

	private int inverse ( int move ) 
	{
	  return move + 2 - 2 * (move % 3);
	}

	//----------------------------------------------------------------------
	private int phase;

	//----------------------------------------------------------------------

	private List<Integer> id (List<Integer> state ) {
	  
	  //--- Phase 1: Edge orientations.
	  if( phase < 2 )
	    return state.subList(20, 32);
	    		//Vector<Integer>( state.begin() + 20, state.begin() + 32 );
	  
	  //-- Phase 2: Corner orientations, E slice edges.
	  if( phase < 3 ){
		  List<Integer> result = state.subList(31, 40);
	    for( int e=0; e<12; e++ )
	      result.set(0, result.get(0)|((state.get(e) / 8) << e));
	    return result;
	  }
	  
	  //--- Phase 3: Edge slices M and S, corner tetrads, overall parity.
	  if( phase < 4 ){
		  List<Integer> result = new ArrayList<Integer>(3);
	    for( int e=0; e<12; e++ )
	    	result.set(0, result.get(0)|(((state.get(e) > 7) ? 2 : (state.get(e) & 1)) << (2*e)));
	    for( int c=0; c<8; c++ )
	    	result.set(1, result.get(1)|(((state.get(c+12)-12) & 5) << (3*c)));
	    for( int i=12; i<20; i++ )
	      for( int j=i+1; j<20; j++ )
	    	  result.set(2, (result.get(2)^(state.get(i)*state.get(i))));
	    return result;
	  }
	  
	  //--- Phase 4: The rest.
	  return state;
	}

	@Override
	public String AutoSolve(String InitialState) {
		String[] initialState = InitialState.split(" ");
		String result = "";
		char[] charindex = {'U', 'D', 'F', 'B', 'L', 'R'};
		
		  //--- Define the goal.
		  String goal[] = { "UF", "UR", "UB", "UL", "DF", "DR", "DB", "DL", "FR", "FL", "BR", "BL",
			    "UFR", "URB", "UBL", "ULF", "DRF", "DFL", "DLB", "DBR" };
		  
		  //--- Prepare current (start) and goal state.
		  List<Integer> currentState = new ArrayList<Integer>( 40 );
		  List<Integer> goalState = new ArrayList<Integer>( 40 );
		  for( int i=0; i<20; i++ ){
		    
		    //--- Goal state.
		    goalState.set(i, i);
		    
		    //--- Current (start) state.
		    String cubie = initialState[i];
		    while(true)
		    {
		    	currentState.set(i, (find(goal, 20, cubie)));
		    	if(currentState.get(i) != 20)
		    	{
		    		break;
		    	}
			    cubie = cubie.charAt(0) + "" + cubie.charAt(0);
			    currentState.set(i+20, currentState.get(i+20)+1);
		    }
		  }
		  
		  //--- Dance the funky Thistlethwaite...
		  while( ++phase < 5 )
		  {
		    
		    //--- Compute ids for current and goal state, skip phase if equal.
		    List<Integer> currentId = id( currentState ), goalId = id( goalState );
		    if( currentId == goalId )
		      continue;
		    
		    //--- Initialize the BFS queue.
		    Queue<List<Integer>> q = new LinkedList<List<Integer>>();
		    q.add( currentState );
		    q.add( goalState );
		    
		    //--- Initialize the BFS tables.
		    Map<List<Integer>,List<Integer>> predecessor = new HashMap<List<Integer>,List<Integer>>();
		    Map<List<Integer>,Integer> direction = new HashMap<List<Integer>,Integer>();
		    Map<List<Integer>,Integer> lastMove = new HashMap<List<Integer>,Integer>();
		    
		    direction.put(currentId, 1);// [ currentId ] = 1;
		    direction.put(goalId, 2);//[ goalId ] = 2;
		    
		    //--- Dance the funky bidirectional BFS...
		    while( true )
		    {
		    	//--- Get state from queue, compute its ID and get its direction.
		    	List<Integer> oldState = q.element();// .front();
		    	q.poll();
		    	List<Integer> oldId = id( oldState );
		    	int oldDir = direction.get(oldId);//	[oldId];
		      
		    	//--- Apply all applicable moves to it and handle the new state.
		    	for( int move=0; move<18; move++ ){
		    	if( (applicableMoves[phase]!=0) & ((1 << move)!=0) )
		    	{
		    		//--- Apply the move.
		    		List<Integer> newState = applyMove( move, oldState );
		    		List<Integer> newId = id( newState );
		    		int newDir = direction.get(newId);//[newId];
			  
		    		//--- Have we seen this state (id) from the other direction already?
		    		//--- I.e. have we found a connection?
		    		if( newDir!=0  &&  newDir != oldDir )
		    		{
			    
			    		//--- Make oldId represent the forwards and newId the backwards search state.
					    if( oldDir > 1 ){
					    	
					      //swap( newId, oldId );
					    	List<Integer> tmp = newId;
					    	newId = oldId;
					    	oldId = tmp;
					    	move = inverse( move );
					    }
				    
					    //--- Reconstruct the connecting algorithm.
					    List<Integer> algorithm = new ArrayList<Integer>(1);
					    algorithm.set(0, move);
					    while( oldId != currentId ){
					    	algorithm.add(0, lastMove.get(oldId));// .insert( algorithm.begin(),  );
					    	oldId = predecessor.get(oldId);//[ oldId ];
					    }
					    while( newId != goalId )
					    {
					    	algorithm.add(inverse( lastMove.get(newId) ));
					    	newId = predecessor.get(oldId);
					    }
				    
					    //--- Print and apply the algorithm.
					    for( int i=0; i<(int)algorithm.size(); i++ ){
					    	result += charindex[algorithm.get(i)/3] +"" + (algorithm.get(i)%3+1);
					    	currentState = applyMove( algorithm.get(i), currentState );
					    }
				    
					    //--- Jump to the next phase.
					    break;
		    		}
			  
					  //--- If we've never seen this state (id) before, visit it.
		    			if( newDir == 0 )
		    			{
		    				q.add(newState);//.push( newState );
						    newDir = oldDir;
						    lastMove.put(newId, move);//[ newId ] = move;
						    predecessor.put(newId, oldId);//[ newId ] = oldId;
		    			}
		    		}
		    	}
		    }
		  }		  

		return result;
	}

	//----------------------------------------------------------------------
	private int find(String[] lst, int range, String target)
	{
		for( int i=0; i<range; i++)
		{
			if(lst[i] == target)
				return i;
		}
		return range;
	}


}
