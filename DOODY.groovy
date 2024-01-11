import com.neuronrobotics.bowlerstudio.vitamins.Vitamins

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.RoundedCube
import eu.mihosoft.vrl.v3d.Sphere
import eu.mihosoft.vrl.v3d.Vector3d
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase
import eu.mihosoft.vrl.v3d.parametrics.IParameterChanged
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter
import eu.mihosoft.vrl.v3d.parametrics.Parameter
import eu.mihosoft.vrl.v3d.parametrics.StringParameter


class SampleMaker implements IParameterChanged{//collection of parts
	ArrayList<CSG> parts = null;
	boolean loading=false;
	ArrayList<CSG> makeSamples(){
		if(parts !=null){
			return parts
		}
		loading=true;
		double myStartSize = 40;
		LengthParameter size 		= new LengthParameter("size",myStartSize,[120.0,1.0])
		LengthParameter smallerSize 		= new LengthParameter("smaller size",myStartSize/20*12.5,[120.0,1.0])
		CSGDatabase.addParameterListener(size.getName(),this);
		CSGDatabase.addParameterListener(smallerSize.getName(),this);
		// force a value to override the database loaded value
		smallerSize.setMM(size.getMM()/20*12.5);

		CSG cube = new Cube(size, size, size).toCSG()
		CSG polygon = Extrude.points(new Vector3d(0, 0, size.getMM()),// This is the  extrusion depth
		                new Vector3d(0,0),// All values after this are the points in the polygon
		                new Vector3d(size.getMM()*2,0),// Bottom right corner
		                new Vector3d(size.getMM()*1.5,size.getMM()),// upper right corner
		                new Vector3d(size.getMM()/2,size.getMM())// upper left corner
		        );		         
		//perform a difference
		// perform union, difference and intersection
		/* CSG cubePlusSphere = cube.union(sphere);
		CSG cubeMinusSphere = cube.difference(sphere);
		CSG cubeIntersectSphere = cube.intersect(sphere); */




		parts = new ArrayList<CSG>();
		int numVits = 0;
		for(String type: Vitamins.listVitaminTypes()){
			String script = Vitamins.getMeta(type).get("scriptGit")
			//println "Type = "+type+" Loading script from "+ script
			for(String s:Vitamins.listVitaminSizes(type) ){
	
				HashMap<String, Object>  vitaminData = Vitamins.getConfiguration( type,s)
				//println "\tSize = "+s+" "+vitaminData
			}
			
			if(script!=null){
			// 	Grab the first vitamin from the list and load that
				println "Loading "+type+" "+Vitamins
				.listVitaminSizes(type)
				.get(0)
				ArrayList<String> options = Vitamins.listVitaminSizes(type);
				CSG lastPart;
				if(parts.size()>0)
					lastPart= parts.get(parts.size()-1)
				else
					lastPart = cube
				StringParameter typParam = new StringParameter(	type+" Default",
														options.get(0),
														options)
				try{
					CSG vitaminFromScript = Vitamins.get( type,typParam.getStrValue())
					if(vitaminFromScript!=null){
						vitaminFromScript=vitaminFromScript
											.toXMax()
											.movex(-size.getMM()*2)
											.toYMin()
											.movey(lastPart.getMaxY()+5)
						CSGDatabase.addParameterListener(typParam.getName(),this);
						vitaminFromScript.setName(typParam.getStrValue())
						numVits++;		
						if(vitaminFromScript!=null){
							parts.add(vitaminFromScript)
							//BowlerStudioController.addCsg(vitaminFromScript)//displays just this item
						}
					}else{
						println type+" "+typParam.getStrValue()+" Failed "
					}
				}catch (Exception ex){
					println type+" "+typParam.getStrValue()+" exception "
				}
			}else
				println "ERROR no script for "+type
		}		
		
		parts.add(cube)
		parts.add(polygon.movex(size.getMM()*5))
		for(int i=0;i<parts.size();i++){
			CSG part=parts.get(i)
			int myIndex=i;
			part.setRegenerate({ 
				makeSamples().get(myIndex)
			})
			.setParameter(size)
		}
		loading=false;
		return parts
	}
	/**
	 * This is a listener for a parameter changing
	 * @param name
	 * @param p
	 */
	 HashMap<String,String> lastValue = new HashMap<>()
	public void parameterChanged(String name, Parameter p){
		//if(p.getStrValue()==null&& p.getValue()==null)
		//	return
		if(loading)
			return
		//if(lastValue.get(name)!=null )
		//	if(p.getStrValue().contains(lastValue.get(name)))
		//		return
		
		lastValue.put(name,p.getStrValue())
		println "CHANGED: "+name
		parts=null
	}
}
CSGDatabase.clear()
return new SampleMaker().makeSamples()
	