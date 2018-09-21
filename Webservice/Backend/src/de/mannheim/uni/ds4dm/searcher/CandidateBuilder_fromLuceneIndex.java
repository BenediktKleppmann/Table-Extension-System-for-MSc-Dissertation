package de.mannheim.uni.ds4dm.searcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.mannheim.uni.ds4dm.model.TableData;
import de.mannheim.uni.ds4dm.normalizer.StringNormalizer;
import de.mannheim.uni.searchjoin.HeaderSearcher;
import de.mannheim.uni.searchjoin.TableFetcher;

public class CandidateBuilder_fromLuceneIndex implements CandidateBuilder {

	private String indexConfigurationFilePath;
	private File folderFile;
	private	HeaderSearcher hs;
	private TableFetcher tf;

	
	
	public CandidateBuilder_fromLuceneIndex() {
		super();
		this.indexConfigurationFilePath = "searchJoins.conf";
		this.hs = new HeaderSearcher();
		this.tf = new TableFetcher();

	}
	
	public CandidateBuilder_fromLuceneIndex(String indexConfigurationFilePath) {
		super();
		this.indexConfigurationFilePath = indexConfigurationFilePath;
		this.hs = new HeaderSearcher(indexConfigurationFilePath);
		this.tf = new TableFetcher(indexConfigurationFilePath);

	}

	public Map<String, TableData> finCandidates(
			DS4DMBasicMatcher qts) {
					
		
		Map<String,TableData> candidates = new HashMap<String,TableData>();
		List<String> att = qts.getExtAtt();
		int max = qts.getMaximalNumberOfTables();
		String [] headers = att.toArray(new String[att.size()]);
		
		//TODO close things that are open
//		Set<String> tableNames = this.hs.searchTablesByHeaders(headers,max); //limit the candidate pool
		List<String> tablesWithCorrespondences = Arrays.asList("1990s_311710_611168.csv.gz", "1990s_311710_611205.csv.gz", "19th_century_245271_478326.csv.gz", "2000s_311710_611239.csv.gz", "20th_century_245271_478409.csv.gz", "Best_All-Time_Series_440541_901784.csv.gz", "Between_10_million_and_20_million_copies_123208_254372.csv.gz", "Between_15_million_and_20_million_copies_123208_254489.csv.gz", "Between_20_million_and_30_million_copies_123208_254358.csv.gz", "Between_20_million_and_30_million_copies_123208_254481.csv.gz", "Between_30_million_and_50_million_copies_123208_254348.csv.gz", "Between_50_million_and_100_million_copies_123208_254341.csv.gz", "Between_50_million_and_100_million_copies_123208_254466.csv.gz", "Bibliography_253959_492858.csv.gz", "Bombers_444876_914639.csv.gz", "Booker_Winners_and_Shortlists_8348_634.csv.gz", "Books_311990_625314.csv.gz", "Books_in_the_black_dustjacket_collection_by_title_136761_262217.csv.gz", "Bulletin_..._film_currently_in_development__Midnight_s_Children_by_Deepa_Mehta_444870_911112.csv.gz", "Bulletin_..._film_ready_for_release__The_Road_by_John_Hillcoat_451489_930915.csv.gz", "Civilians___444876_915557.csv.gz", "Complete_list_of_Classics_Illustrated_comic_books__original_US_run__120939_254361.csv.gz", "Editor_344629_680916.csv.gz", "Editors__list__20th_Century_Great_Novels__199790_372764.csv.gz", "Enterprise__2001-present__49140_94900.csv.gz", "Example_193590_373517.csv.gz", "Examples_of_books_with_Lexile_measures_27__458441_928086.csv.gz", "Hardcover_titles_26739_40828.csv.gz", "Historical_development_66109_133589.csv.gz", "Homefront_451351_924792.csv.gz", "List_230789_461343.csv.gz", "List_of_Zephyr_Books_522975_1090132.csv.gz", "List_of_longest_novels_69419_128239.csv.gz", "List_of_the_100_Best_Books_of_All_Time_546013_1141406.csv.gz", "Literature_456532_932604.csv.gz", "Literature_47671_85092.csv.gz", "More_than_100_million_copies_123208_254333.csv.gz", "Synopsis_and_format_497288_1023900.csv.gz", "The_100_Books_of_the_Century_542614_1141780.csv.gz", "The_List_564839_1203790.csv.gz", "Time_travel_in_novels_and_short_stories_271832_525600.csv.gz", "Top_twelve_441369_898856.csv.gz", "Volumes_in_the_Leatherbound_Classics_collection_136761_262394.csv.gz", "Winners_176696_332265.csv.gz", "Winners_301641_601976.csv.gz", "Winners_353671_714410.csv.gz", "Winners_575_10685.csv.gz", "Winners_8293_15694.csv.gz", "Winners_and_nominees_29159_39735.csv.gz", "Winners_and_nominees_34294_62253.csv.gz", "Winners_and_nominees_34348_64129.csv.gz", "Winners_and_nominees_34355_64409.csv.gz", "Winners_and_nominees_34365_65023.csv.gz", "_100954_207359.csv.gz", "_239188_445656.csv.gz", "_341289_685894.csv.gz", "_448402_904334.csv.gz", "_479146_988432.csv.gz", "_601850_1306618.csv.gz");
		Set<String> tableNames = this.hs.searchTablesByHeaders(headers);
		
		for (String t:tableNames){
			//candidates.put(t.getName(),table);
			//TODO load a table 
			if (tablesWithCorrespondences.contains(t)){
				System.out.println("one of the corresponding tables was found: " + t);
			}
			
			

			TableData table = this.tf.getTableData(t);
//			TableData table = loadTable(this.tf, t);
			candidates.put(t,table);
		}
		
		return candidates;
	}
	
	

//	private TableData loadTable(TableFetcher hs, String t) {
//
//		TableData tab = new TableData();
//		try {
//			tab.setHasHeader(true);
//			tab.setHasKeyColumn(true);
//			tab.setRelation(hs.getRowValues(t));
//		} catch (Exception e) {
//			System.err.println("error loading table "+t);
//			e.printStackTrace();
//			return null;
//		}
//		return tab;
//	}

	private boolean headersMatch(String[] head, List<String> att) {
		List<String> normalizedHeaders = new ArrayList<String>();
		List<String> normalizedTarget = new ArrayList<String>();

		for (String s: head){
			normalizedHeaders.add(StringNormalizer.format(s));
		}
		for (String s: att){
			normalizedTarget.add(StringNormalizer.format(s));
		}
		
		for (String s: normalizedHeaders){
			if (normalizedTarget.contains(s))
				return true;
		}
		
		return false;
	}





	@Override
	public Set<TableData> finCandidates(Matcher matcher) {
		// TODO Auto-generated method stub
		return null;
	}



}
