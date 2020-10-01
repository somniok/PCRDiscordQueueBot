package net.somniok.pcr.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.somniok.pcr.main.Bot;
import net.somniok.pcr.object.Backup;
import net.somniok.pcr.object.BossData;
import net.somniok.pcr.object.BossList;
import net.somniok.pcr.object.BossQueue;
import net.somniok.pcr.object.Report;
import net.somniok.pcr.object.ReportList;

public class ExcelWriter {

	static String [] backupRotation = new String[6];
	static int slot = 0;

	public static void init() {
		for(int i=0;i<backupRotation.length;i++) {
			backupRotation[i] = "";
		}
		
	}

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	private static final String filePath = "./";
	public static void writeReport(Map<String, Map<LocalDate, ReportList>> usersMap, Map<String, String> userNameMap) {
		try {
			FileOutputStream out = new FileOutputStream( filePath + "PCR"+formatter.format(LocalDateTime.now())+".xlsx");
			Workbook wb = new XSSFWorkbook();
			LocalDate currDate = LocalDate.now();
			Sheet s = wb.createSheet(currDate.getYear() + " " + currDate.getMonthValue());
			int r = 0;
			Row headerRow = s.createRow(r++);
			Row headerRow2 = s.createRow(r++);
			CellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			
			CellStyle secondBossStyle = wb.createCellStyle();
			secondBossStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			secondBossStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle sumStyle = wb.createCellStyle();
			sumStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
			sumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			

			CellStyle warnStyle = wb.createCellStyle();
			warnStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			warnStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			

			CellStyle mergeStyle = wb.createCellStyle();
			mergeStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);
			mergeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			

			Cell tempCell;
			int damage, point;
			int c = 2;
			for(String userId : usersMap.keySet()) {
				tempCell = headerRow.createCell(c);
				tempCell.setCellValue(userNameMap.get(userId));
				s.addMergedRegion(new CellRangeAddress(0,0,c,c+2));
				tempCell.setCellStyle(mergeStyle);
				
				tempCell = headerRow2.createCell(c++);
				tempCell.setCellValue("Boss");
				tempCell.setCellStyle(headerStyle);

				tempCell = headerRow2.createCell(c++);
				tempCell.setCellValue("Damage");
				tempCell.setCellStyle(headerStyle);

				tempCell = headerRow2.createCell(c++);
				tempCell.setCellValue("Point");
				tempCell.setCellStyle(headerStyle);
	
			}
			
			LocalDate date = Bot.startDate;
			Row[] rows = new Row[7];
			for(int i=0;i<BossData.totalDays;i++) {
				int initRow = r;
				for(int j=0;j<7;j++) {
					rows[j] = s.createRow(r++);
				}
				tempCell = rows[0].createCell(0);
				tempCell.setCellValue(date.toString());
				s.addMergedRegion(new CellRangeAddress(initRow,initRow+5,0,0));
				tempCell.setCellStyle(mergeStyle);
				
				
				tempCell = rows[0].createCell(1); 
				tempCell.setCellValue("一般刀");
				s.addMergedRegion(new CellRangeAddress(initRow,initRow+2,1,1));
				tempCell.setCellStyle(mergeStyle);

				tempCell = rows[3].createCell(1);
				tempCell.setCellValue("補償刀");
				s.addMergedRegion(new CellRangeAddress(initRow+3,initRow+5,1,1));
				tempCell.setCellStyle(mergeStyle);
				
				tempCell = rows[6].createCell(1);
				tempCell.setCellValue("總合");
				tempCell.setCellStyle(sumStyle);
				
				s.addMergedRegion(new CellRangeAddress(initRow+6,initRow+6,0,1));
				c = 2;
				if(date.compareTo(ReportList.currDate) > 0) {
					date = date.plusDays(1);
					continue;
				}
				for(Map<LocalDate, ReportList> userReportMap : usersMap.values()) {
//					if(userReportMap.isEmpty()) System.out.println("empty set");
//					userReportMap.forEach((k,v)->System.out.println(k.toString() + " " + v.listReport(" ")));
					if(userReportMap.containsKey(date)) {
						ReportList rl = userReportMap.get(date);
//						System.out.println("inner: " + rl.listReport(" "));
						int upper = 0, lower = 3;
						int totalDamage = 0, totalPoint = 0;
						for(Report report : rl) {
							damage = report.getActualDamage();
							totalDamage += damage;
							point = report.getActualDamage() 
									* BossData.getBossRatio(report.getRound(), report.getBoss()) / 10;
							totalPoint += point;
							if(report.isSecondStatus()) {
								tempCell = rows[lower].createCell(c);
								tempCell.setCellValue(report.getRound() + "-" + report.getBoss());
								tempCell.setCellStyle(secondBossStyle);
								rows[lower].createCell(c+1).setCellValue(damage);
								rows[lower].createCell(c+2).setCellValue(point);
								lower++;
							} else {
								tempCell = rows[upper].createCell(c);
								tempCell.setCellValue(report.getRound() + "-" + report.getBoss());
								rows[upper].createCell(c+1).setCellValue(report.getActualDamage());
								rows[upper].createCell(c+2).setCellValue(point);
								upper++;
							}
						}
						for(;upper<3;upper++) {
							tempCell = rows[upper].createCell(c);
							tempCell.setCellValue("缺");
							tempCell.setCellStyle(warnStyle);
						}
						tempCell = rows[6].createCell(c+1);
						tempCell.setCellValue(totalDamage);
						tempCell.setCellStyle(sumStyle);
						
						tempCell = rows[6].createCell(c+2);
						tempCell.setCellValue(totalPoint);
						tempCell.setCellStyle(sumStyle);
						
					} else {
						tempCell = rows[0].createCell(c);
						tempCell.setCellValue("缺");
						tempCell.setCellStyle(warnStyle);
						tempCell = rows[1].createCell(c);
						tempCell.setCellValue("缺");
						tempCell.setCellStyle(warnStyle);
						tempCell = rows[2].createCell(c);
						tempCell.setCellValue("缺");
						tempCell.setCellStyle(warnStyle);
						
						tempCell = rows[6].createCell(c);
						tempCell.setCellValue("缺");
						tempCell.setCellStyle(warnStyle);
					}
					c += 3;
				}
				rows[6].setRowStyle(sumStyle);
				date = date.plusDays(1);
			}
			for(short i=0;i<headerRow2.getLastCellNum();i++) {
				s.autoSizeColumn(i);
			}
			wb.write(out);
			out.close();
			wb.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeBackup(Map<String, Map<LocalDate, ReportList>> usersMap, Map<String, String> userNameMap,
			BossQueue[] oneHitBossQueues, BossQueue[] bossQueues) {

		try {
			File dir = new File(filePath + "PCRBackup");
			if(!dir.exists()) {
				dir.mkdirs();
			}
			String fileName = filePath + "PCRBackup/PCRBackup"+formatter.format(LocalDateTime.now())+".xlsx";
			FileOutputStream out = new FileOutputStream(fileName);
			Workbook wb = new XSSFWorkbook();
			Sheet userSheet = wb.createSheet("userMap");

			Row row;
			int r = 0;
			if(userNameMap != null) {
				//Map.Entry<String, String> entry : map.entrySet()
				for(Map.Entry<String, String> e : userNameMap.entrySet()) {
					row = userSheet.createRow(r++);
					row.createCell(0).setCellValue(e.getKey());
					row.createCell(1).setCellValue(e.getValue());
				}
			}
			Sheet oneHitQueueSheet = wb.createSheet("oneHitQueue");
			
			r=0;
			int c=0;
			for(BossQueue oneHitQueue : oneHitBossQueues) {
				row = oneHitQueueSheet.createRow(r++);
				for(String id : oneHitQueue.getQueue()) {
					row.createCell(c++).setCellValue(id);;
				}
				c=0;
			}
			
			Sheet bossQueueSheet = wb.createSheet("bossQueue");
			r=0;
			c=0;
			for(BossQueue bossQueue : bossQueues) {
				row = bossQueueSheet.createRow(r++);
				for(String id : bossQueue.getQueue()) {
					row.createCell(c++).setCellValue(id);;
				}
				c=0;
			}

			Sheet mainSheet = wb.createSheet("mainSheet");

			if(usersMap != null) {
				String userId;
				LocalDate currDate;
				r=0;
				for(Map.Entry<String, Map<LocalDate, ReportList>> userReportLists : usersMap.entrySet()) {
					userId = userReportLists.getKey();
					for(Map.Entry<LocalDate, ReportList> reportListEn : userReportLists.getValue().entrySet()) {
						currDate = reportListEn.getKey();
						for(Report report : reportListEn.getValue()) {
							row =  mainSheet.createRow(r++);
							row.createCell(0).setCellValue(userId);
							row.createCell(1).setCellValue(currDate);
							row.createCell(2).setCellValue(report.getRound());
							row.createCell(3).setCellValue(report.getBoss());
							row.createCell(4).setCellValue(report.getActualDamage());
							row.createCell(5).setCellValue(report.getUserName());
							row.createCell(6).setCellValue(report.getType().name());
							row.createCell(7).setCellValue(report.getStatus().name());
						}
					}
				}
			}
			
			wb.write(out);
			out.close();
			wb.close();
			
			if(!backupRotation[slot].isEmpty()) {
				File oldFile = new File(backupRotation[slot]);
				if(oldFile.exists()) {
					oldFile.delete();
				}
			}
			backupRotation[slot] = fileName;
			
			slot++;
			if(slot >= backupRotation.length) {
				slot = 0;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Backup readBackup(String fileName) {
		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(filePath + "PCRBackup/" + fileName));
			Sheet userSheet = wb.getSheet("userMap");
			Map<String, String> userNameMap = new ConcurrentSkipListMap<String, String>();
			for (Row row : userSheet) {
				userNameMap.put(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue());
			}

			
			Sheet oneHitSheet = wb.getSheet("userMap");
			BossQueue[] oneHitBossQueues = new BossQueue[5];
			int r = 0;
			for (Row row : oneHitSheet) {
				oneHitBossQueues[r] = new BossQueue();
				for(Cell cell : row) {
					oneHitBossQueues[r].add(cell.getStringCellValue());
				}
				if(++r >= 5) {
					break;
				}
			}
			
			Sheet bossSheet = wb.getSheet("bossQueue");
			BossQueue[] bossQueues = new BossQueue[5];
			r = 0;
			for (Row row : bossSheet) {
				oneHitBossQueues[r] = new BossQueue();
				for(Cell cell : row) {
					bossQueues[r].add(cell.getStringCellValue());
				}
				if(++r >= 5) {
					break;
				}
			}
			
			Sheet mainSheet = wb.getSheet("mainSheet");
			Map<String, Map<LocalDate, ReportList>> usersMap = new ConcurrentSkipListMap<String, Map<LocalDate, ReportList>>();
			Map<Integer, BossList> checksumMap = new ConcurrentSkipListMap<Integer, BossList>();
			Map<LocalDate, ReportList> tempUserRL;
			ReportList tempRL;
			BossList tempBL;
			for(Row row : mainSheet) {
				if(usersMap.containsKey(row.getCell(0).getStringCellValue())){
					tempUserRL = usersMap.get(row.getCell(0).getStringCellValue());
				} else {
					tempUserRL = new ConcurrentSkipListMap<LocalDate, ReportList>();
				}
				
				LocalDate tempLocalDate = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
				
				if(tempUserRL.containsKey(tempLocalDate)){
					tempRL = tempUserRL.get(tempLocalDate);
				} else {
					tempRL = new ReportList(row.getCell(5).getStringCellValue());
					tempUserRL.put(tempLocalDate, tempRL);
				}
				Report report = new Report((int) row.getCell(2).getNumericCellValue(),
						(int) row.getCell(3).getNumericCellValue(), Report.HitType.valueOf(row.getCell(6).getStringCellValue()),
						(int) row.getCell(4).getNumericCellValue(), Report.HitStatus.valueOf(row.getCell(7).getStringCellValue()),
						row.getCell(5).getStringCellValue(), tempLocalDate);
				tempRL.add(report);
				
				Integer key = report.getRound() * 10 + report.getBoss();
				if(checksumMap.containsKey(key)) {
					tempBL = checksumMap.get(key);
				} else {
					tempBL = new BossList(report.getRound(), report.getBoss());
					checksumMap.put(key, tempBL);
				}
				tempBL.add(report);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
}
