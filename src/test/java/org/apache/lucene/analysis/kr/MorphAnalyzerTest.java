package org.apache.lucene.analysis.kr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.kr.morph.AnalysisOutput;
import org.apache.lucene.analysis.kr.morph.CompoundEntry;
import org.apache.lucene.analysis.kr.morph.CompoundNounAnalyzer;
import org.apache.lucene.analysis.kr.morph.MorphAnalyzer;
import org.apache.lucene.analysis.kr.morph.MorphAnalyzerManager;
import org.apache.lucene.analysis.kr.morph.WordEntry;
import org.apache.lucene.analysis.kr.utils.DictionaryUtil;

import junit.framework.TestCase;

public class MorphAnalyzerTest extends TestCase {


	public void testMorphAnalyzer() throws Exception {
		
		String[] inputs = new String[] {
				//"고물가시대의",
				//"대외적",
				//"합쳐져","뛰어오르고","급여생활자나","영세자영업자","영세농어민","서민계층들은","온몸으로","엄습하고",
				//"드라마가",
				//"과장광고","과소비",
				//"날을","아울러","휴대전화기능처리부와","코발트의",
				//"달라","포함하고",
//				"사랑받아봄을","하는", 
				"정답은",
				"우습을",
				"사용하여"
				// ,"발행일","출원인"	// 	어미로 끝나는 경우로 분석된다.	
				//,"노란",
				//"만능청약통장","가시밭같다",
				//"정책적",	"시리즈를","자리잡은","찜통이다","지난해",
				//"데모입니다",
				//"바이오및뇌공학",
				//"급락조짐을",
				//"4.19의거는",
				//"고스트x를",
				//"검색서비스를",
				//"장애물이"
				};
		
		MorphAnalyzer analyzer = new MorphAnalyzer();
		long start = 0;
		for(String input:inputs) {
			List<AnalysisOutput> list = analyzer.analyze(input);
			for(AnalysisOutput o:list) {
				System.out.print(o.toString()+"->");
				for(int i=0;i<o.getCNounList().size();i++){
					System.out.print(o.getCNounList().get(i).getWord()+"/");
				}
				System.out.print(o.getPatn());
				System.out.println("<"+o.getScore()+">");
			}
			if(start==0) start = System.currentTimeMillis();
		}
		System.out.println((System.currentTimeMillis()-start)+"ms");
	}
		
	public void testCloneAnalysisOutput() throws Exception {
		AnalysisOutput output = new AnalysisOutput();
		
		output.setStem("aaaa");
		
		AnalysisOutput clone = output.clone();
		
		assertEquals("aaaa", clone.getStem());
		
		System.out.println(clone.getStem());
	}
	
	
	public void testMorphAnalyzerManager() throws Exception {
		String input = "나는 학교에 갔습니다";

		MorphAnalyzerManager manager = new MorphAnalyzerManager();
		manager.analyze(input);
	}
	
	public void testAlphaNumeric() throws Exception {
		String str = "0123456789azAZ";
		for(int i=0;i<str.length();i++) {
			System.out.println(str.charAt(i)+":"+(str.charAt(i)-0));
		}
	}
	
	public void testGetWordEntry() throws Exception {
		String s = "밤하늘";
		WordEntry we = DictionaryUtil.getCNoun(s);
		System.out.println(we.getWord());
	}
	
	/**
	 * 세종사전에서 하다와 되다형 동사를 체언과 결합하기 위해 사용한 테스트케이스
	 * 
	 * @throws Exception
	 */
	public void testYongonAnalysis() throws Exception {
		
		String fname = "data/용언_상세.txt";
		
		List<String> list = FileUtils.readLines(new File(fname));
		Map<String, String> younons = new HashMap();
		
		MorphAnalyzer analyzer = new MorphAnalyzer();
		long start = 0;
		List youngOutputs = new ArrayList();
		for(String input:list) {
			
			if(!input.endsWith("하다")&&!input.endsWith("되다")) {
				youngOutputs.add(input);
				continue;			
			}
			String eogan = input.substring(0,input.length()-2);
			
			List<AnalysisOutput> outputs = analyzer.analyze(input);
			AnalysisOutput o = outputs.get(0);
			String result = o.toString()+"->";
			for(int i=0;i<o.getCNounList().size();i++){
				result += o.getCNounList().get(i).getWord()+"/";
			}
			result += "<"+o.getScore()+">";

			String tmp = younons.get(eogan);
			if(tmp==null) {
				younons.put(eogan, result);
			} else {
				younons.put(eogan, tmp+"| "+result);
			}
		}
		
		fname = "data/체언_상세.txt";		
		String cheonOutfile = "data/cheon.txt";
		String youngOutfile = "data/youngon.txt";
		
		List<String> cheons = FileUtils.readLines(new File(fname));	
		List<String> outputs = new ArrayList();
		System.out.println(younons.size());		
		for(String cheon : cheons) {
			String str = younons.remove(cheon);
			if(str!=null) {
				cheon += "=> "+str;
//				younons.remove(cheon);
			}
			outputs.add(cheon);
		}

		Iterator<String> iter = younons.keySet().iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			outputs.add(key+"=> " + younons.get(key));
		}
		
		Collections.sort(outputs);
		Collections.sort(youngOutputs);
		
		FileUtils.writeLines(new File(cheonOutfile), outputs);
		FileUtils.writeLines(new File(youngOutfile), youngOutputs);
		
		outputs.addAll(youngOutputs);
		Collections.sort(outputs);
		FileUtils.writeLines(new File( "data/all.txt"), outputs);
	}
	
	public void testCompoundNounsWithinDic() throws Exception {		
		
		String input = "고투자율";

		   WordEntry cnoun = DictionaryUtil.getCNoun(input);
		   List<CompoundEntry> list = null;
		   if(cnoun!=null && cnoun.getFeature(WordEntry.IDX_NOUN)=='2') {
			   list = cnoun.getCompounds();
			   
			   for(int j=0;j<list.size();j++) {
				   System.out.println(list.get(j).getWord());
			   }
		   }	
		   
	}
	
	public void testCompoundNouns() throws Exception {		
		
		 String input = "가돌리늄착화합물";
		 CompoundNounAnalyzer cnAnalyzer = new CompoundNounAnalyzer();	
		 cnAnalyzer.setExactMach(false);
		 
		  List<CompoundEntry> list = cnAnalyzer.analyze(input);
		  if(list==null) return;
		  
		  for(int i=0;i<list.size();i++) {
			  System.out.println(list.get(i).getWord());
		  }

	}
}

