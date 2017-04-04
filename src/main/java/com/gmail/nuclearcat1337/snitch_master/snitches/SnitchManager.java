package com.gmail.nuclearcat1337.snitch_master.snitches;

import com.gmail.nuclearcat1337.snitch_master.SnitchMaster;
import com.gmail.nuclearcat1337.snitch_master.api.SnitchListQualifier;
import com.gmail.nuclearcat1337.snitch_master.locatableobjectlist.LocatableObjectList;
import com.gmail.nuclearcat1337.snitch_master.util.Color;

import java.io.*;
import java.util.*;

/**
 * Created by Mr_Little_Kitty on 3/10/2017.
 */
public class SnitchManager
{
	private static final SnitchListQualifier friendly = new SnitchListQualifier("origin == 'jalist' || origin == 'chat'");
	private static final SnitchListQualifier neutral = new SnitchListQualifier("origin == 'manual'");

	public static SnitchList[] getDefaultSnitchLists(SnitchManager manager)
	{
		return new SnitchList[]{ new SnitchList(manager,SnitchManager.friendly, true, "Friendly",new Color(0, (int)(0.56D*255D), 255)),
				new SnitchList(manager,SnitchManager.neutral, true, "Neutral",new Color(238, 210, 2))}; //"Safety Yellow"
	}

	private static final String modSnitchesFile = SnitchMaster.modDataFolder+"/Snitches.csv";
	private static final String modSnitchListsFile = SnitchMaster.modDataFolder+"/SnitchLists.csv";

	private final SnitchMaster snitchMaster;

	private final LocatableObjectList<Snitch> snitches;
	private final List<SnitchList> snitchLists;

	private boolean globalRender;

	public SnitchManager(SnitchMaster snitchMaster)
	{
		this.snitchMaster = new SnitchMaster();

		snitches = new LocatableObjectList<>();
		snitchLists = new ArrayList<>();

		globalRender = false;

		loadSnitchLists(new File(modSnitchListsFile));
		loadSnitches(new File(modSnitchesFile));

		if(snitchLists.isEmpty()) //If we load the lists from the file and there are none, create the default ones
		{
			for(SnitchList list : getDefaultSnitchLists(this))
				snitchLists.add(list);
		}
	}

	public SnitchList getRenderListForSnitch(Snitch snitch)
	{
		for(SnitchList list : snitch.attachedSnitchLists)
			if(list.shouldRenderSnitches())
				return list;

		return null;
	}

	public List<SnitchList> getSnitchListsForSnitch(Snitch snitch)
	{
		return snitch.attachedSnitchLists;
	}

	public boolean doesListWithNameExist(String name)
	{
		for (SnitchList list : snitchLists)
		{
			if(list.getListName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public SnitchList createSnitchList(String name, SnitchListQualifier qualifier, boolean render, Color color)
	{
		if(doesListWithNameExist(name))
			return null;

		SnitchList list = new SnitchList(this,qualifier,render,name,color);

		for(Snitch snitch : getSnitches())
			if(qualifier.isQualified(snitch))
				attachListToSnitch(list,snitch);

		snitchLists.add(list);

		saveSnitchLists();

		return list;
	}

	public boolean removeSnitchList(String name)
	{
		if(!doesListWithNameExist(name))
			return false;

		for(int i = 0; i < snitchLists.size(); i++)
		{
			if (snitchLists.get(i).getListName().equalsIgnoreCase(name))
			{
				SnitchList list = snitchLists.remove(i);

				//Now we need to update the render priority of all the remaining snitch lists
				//And remove any references to this list from the attached snitch lists
				for(int j = 0; j < snitchLists.size(); j++)
					snitchLists.get(j).setRenderPriorityUnchecked(j+1);

				for(Snitch snitch : getSnitchesInList(list))
					snitch.attachedSnitchLists.remove(list);

				saveSnitchLists();

				return true;
			}
		}
		return false;
	}

	public void toggleGlobalRender()
	{
		this.globalRender = !this.globalRender;
	}

	public boolean getGlobalRender()
	{
		return globalRender;
	}

	public ArrayList<Snitch> getSnitchesInList(SnitchList list)
	{
		ArrayList<Snitch> attachedSnitches = new ArrayList<>();
		for(Snitch snitch : snitches)
		{
			if(snitch.attachedSnitchLists.contains(list))
				attachedSnitches.add(snitch);
		}
		return attachedSnitches;
	}

	/**
	 * Submits a Snitch for processing and adding to the Snitch collection.
	 * The Snitch is added to all SnitchLists, JourneyMap, (if applicable) and then saved to a file.
	 */
	public void submitSnitch(Snitch snitch)
	{
		//Check to see if there is already a snitch at this location
		Snitch contains = snitches.get(snitch.getLocation());

		//Check if the snitch that was submitted already exists
		if(contains != null)
		{
			//If it does then change the cull time and group
			contains.setCullTime(snitch.getCullTime());
			contains.setGroupName(snitch.getGroupName());
			contains.setSnitchName(snitch.getSnitchName());

			//Clear the attached snitch lists because we are going to requalify the snitch because some attributes changed
			contains.attachedSnitchLists.clear();
		}
		else
		{
			//Just some reference rearranging
			contains = snitch;
			//add the snitch to the collection
			snitches.add(contains);
		}

		//Go through all the snitch lists to see if this snitch should be in them
		for(SnitchList list : snitchLists)
		{
			//If it should then attach the snitch list to the snitch
			if(list.getQualifier().isQualified(contains))
				attachListToSnitch(list,contains);
		}

		//send it to journey map if that is enabled
		snitchMaster.individualJourneyMapUpdate(contains);
	}

	public void saveSnitchLists()
	{
		ArrayList<String> csvs = new ArrayList<>();
		for(SnitchList list : snitchLists)
			csvs.add(SnitchList.ConvertSnitchListToCSV(list));

		writeToCSV(new File(modSnitchListsFile),csvs);
	}

	public void saveSnitches()
	{
		ArrayList<String> csvs = new ArrayList<>();
		for(Snitch snitch : snitches)
			csvs.add(Snitch.ConvertSnitchToCSV(snitch));

		writeToCSV(new File(modSnitchesFile),csvs);
	}

	public LocatableObjectList<Snitch> getSnitches()
	{
		return snitches;
	}

	public Collection<SnitchList> getSnitchLists()
	{
		return snitchLists;
	}

	void journeyMapRedisplay(SnitchList list)
	{
		snitchMaster.snitchListJourneyMapUpdate(list);
	}

	void changeListRenderPriority(SnitchList list, boolean increase)
	{
		int index = snitchLists.indexOf(list);
		int targetIndex = increase ? index-1 : index+1;

		//If they were successfully swapped
		if(swapIfPossible(snitchLists,index,targetIndex))
		{
			//Update the list objects actual render priority
			snitchLists.get(index).setRenderPriorityUnchecked(index+1);
			snitchLists.get(targetIndex).setRenderPriorityUnchecked(targetIndex+1);

			for(Snitch snitch : getSnitchesInList(list))
			{
				//TODO---What we need to do is just sort the attached snitch lists by their render priority
				Collections.sort(snitch.attachedSnitchLists,listComparator);
			}
		}
	}

	void requalifyList(SnitchList list)
	{
		for(Snitch snitch : getSnitchesInList(list))
			snitch.attachedSnitchLists.remove(list);

		SnitchListQualifier qualifier = list.getQualifier();
		for(Snitch snitch : getSnitches())
			if(qualifier.isQualified(snitch))
				attachListToSnitch(list,snitch);

		snitchMaster.fullJourneyMapUpdate();
	}

	private void attachListToSnitch(SnitchList list, Snitch snitch)
	{
		List<SnitchList> attached = snitch.attachedSnitchLists;
		int i = 0;
		for(; i < attached.size(); i++)
		{
			if(list.getRenderPriority() < attached.get(i).getRenderPriority())
				break;
		}
		attached.add(i,list);
	}

	//True if they were swapped
	//False otherwise
	private static <T> boolean swapIfPossible(List<T> list, int index, int targetIndex)
	{
		if(list.size() < 2) //Cant swap if there are less than 2 items
			return false;

		//Make sure both the indices are valid for the list
		if(index >= 0 && index < list.size() && targetIndex >= 0 && targetIndex < list.size())
		{
			T temp = list.get(targetIndex);
			list.set(targetIndex,list.get(index));
			list.set(index,temp);
			return true;
		}
		return false;
	}

	private static void writeToCSV(File file, List<String> lines)
	{
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return;
			}
		}

		try(FileWriter writer = new FileWriter(file))
		{
			for(String line : lines)
			{
				writer.write(line);
				writer.write(System.lineSeparator());
			}
			writer.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void loadSnitches(File file)
	{
		try
		{
			if (file.exists())
			{
				try (BufferedReader br = new BufferedReader(new FileReader(file)))
				{
					for (String line = null; (line = br.readLine()) != null; )
					{
						Snitch snitch = Snitch.GetSnitchFromCSV(line);
						if (snitch != null)
							submitSnitch(snitch);
					}
				}
			}
		}
		catch (IOException e)
		{

		}
	}

	private void loadSnitchLists(File file)
	{
		try
		{
			if (file.exists())
			{
				try (BufferedReader br = new BufferedReader(new FileReader(file)))
				{
					for (String line = null; (line = br.readLine()) != null; )
					{
						SnitchList list = SnitchList.GetSnitchListFromCSV(line,this);
						if(list != null)
							this.snitchLists.add(list);
					}
				}
			}
		}
		catch (IOException e)
		{

		}
	}

	/**
	 * A comparator that sorts SnitchLists according to their render priorities.
	 */
	private static class SnitchListComparator implements Comparator<SnitchList>
	{
		@Override
		public int compare(SnitchList one, SnitchList two)
		{
			return Integer.compare(one.getRenderPriority(),two.getRenderPriority());
		}
	}

	/**
	 * A static instance of the SnitchList comparator to use in all instances of the Snitch class.
	 */
	private static final SnitchListComparator listComparator = new SnitchListComparator();
}
