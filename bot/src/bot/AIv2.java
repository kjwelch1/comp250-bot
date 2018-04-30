/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bot;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */

public class AIv2 extends AbstractionLayerAI {
protected UnitTypeTable utt;
UnitType workerType;
UnitType baseType;

public AIv2 (UnitTypeTable a_utt) {
    this(a_utt, new AStarPathFinding());}

    
public AIv2 (UnitTypeTable a_utt, PathFinding a_pf) {
    super(a_pf);
    reset(a_utt);}

public void reset() {
	super.reset();}

public void reset(UnitTypeTable a_utt)  {
    utt = a_utt;
    if (utt!=null) {
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");}
}   


public AI clone() {
    return new AIv2(utt, pf);
}
   
    
    @Override
    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        PlayerAction pa = new PlayerAction();
    	
     // behavior for the bases
        for(Unit u:pgs.getUnits()) {
            if (u.getType()==baseType && u.getPlayer() == player && gs.getActionAssignment(u)==null) {
                baseBehavior(u,p,pgs);}
        }
        
     // behavior for the workers
        List<Unit> workers = new LinkedList<Unit>();
        for(Unit u:pgs.getUnits()) {
            if (u.getType().canHarvest && 
                u.getPlayer() == player) {
                workers.add(u);
                meleeUnitBehavior(u,p,gs);}        
        }
        
     // behavior for the melee units
        for(Unit u:pgs.getUnits()) {
            if (u.getType().canAttack && !u.getType().canHarvest && u.getPlayer() == player && gs.getActionAssignment(u)==null) {
                meleeUnitBehavior(u,p,gs);}        
        }
        
        return translateActions(player,gs);
    }
    
    public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for(Unit u2:pgs.getUnits()) {
            if (u2.getPlayer()>=0 && u2.getPlayer()!=p.getID()) { 
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy==null || d<closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;}
            }
        }
        if (closestEnemy!=null) {
            attack(u,closestEnemy);}
    }
    
    public void workersBehavior(List<Unit> workers, Player p, GameState gs) {
    	
	}


	public void baseBehavior(Unit u,Player p, PhysicalGameState pgs) {
        if (p.getResources()>=workerType.cost) train(u, workerType);
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
    
}
