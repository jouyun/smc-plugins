package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class aaImageTestMay13corrected implements PlugIn {

	int[] centerx;
	int[] centery;
	//int[] centerZ;
	int[] lex;
	int[] ley;
	//int[] leZ;

	public void run(String arg) {
		int width=2355;
		int height=350;
		int frames = 191;
		
		doGetLocationArrays();
		for(int i=0;i<frames;i++){
			IJ.log(""+i);
			//get spots
			int cx = centerx[i];
			int cy = centery[i];
			int lx = lex[i];
			int ly = ley[i];
			
			float[] pixels=new float[width*height];
			//float[] pix_le=new float[width*height];
			
			//do Center of Mass spot
			for(int j=-10;j<10;j++){
				for(int k=-10;k<10;k++){
					
					int val_center=(cx+j)+(cy+k)*width;
					int val_le=lx+ly*width+j+k*width;
					if(val_center>0 && val_center<width*height){
						pixels[val_center] = 1.0f;
					}
					if(val_le>0 && val_le<width*height){
						pixels[val_le]=1.0f;
					}
				}
			}
			FloatProcessor fp1 = new FloatProcessor(width,height,pixels);
			ImagePlus imp1 = new ImagePlus("track", fp1);
			imp1.show();
			//ImagePlus iplus = new ImagePlus();
			//ImageProcessor ip= iplus.getProcessor();
			//ip.setPixels(1,fp1);
			//stack.addSlice(fp1);
		}
		////FloatProcessor fp1 = new FloatProcessor(width,height,pixels1,null);
		// ImagePlus imp1 = new ImagePlus("Center and LE",stack);
		// imp1.show();

		// FloatProcessor fp2 = new FloatProcessor(width,height,pixels2,null);
		// ImagePlus imp2 = new ImagePlus("Random Dots 2",fp2);
		// imp2.show();

		// FloatProcessor fp3 = new FloatProcessor(width,height,pixels3,null);
		// ImagePlus imp3 = new ImagePlus("Random Dots 3",fp3);
		// imp3.show();

	}

	public void doGetLocationArrays(){
		centerx = new int[253];
		centery = new int[253];
		//centerz = new int[253];
		lex = new int[253];
		ley = new int[253];
		//lez = new int[253];

lex[1]=430;
lex[2]=436;
lex[3]=443;
lex[4]=451;
lex[5]=456;
lex[6]=464;
lex[7]=472;
lex[8]=481;
lex[9]=489;
lex[10]=497;
lex[11]=507;
lex[12]=519;
lex[13]=529;
lex[14]=542;
lex[15]=552;
lex[16]=562;
lex[17]=573;
lex[18]=584;
lex[19]=594;
lex[20]=603;
lex[21]=615;
lex[22]=628;
lex[23]=644;
lex[24]=657;
lex[25]=672;
lex[26]=686;
lex[27]=698;
lex[28]=713;
lex[29]=730;
lex[30]=747;
lex[31]=761;
lex[32]=776;
lex[33]=793;
lex[34]=810;
lex[35]=827;
lex[36]=803;
lex[37]=816;
lex[38]=830;
lex[39]=842;
lex[40]=855;
lex[41]=870;
lex[42]=883;
lex[43]=896;
lex[44]=908;
lex[45]=922;
lex[46]=934;
lex[47]=947;
lex[48]=958;
lex[49]=974;
lex[50]=988;
lex[51]=1004;
lex[52]=1019;
lex[53]=1031;
lex[54]=1043;
lex[55]=1057;
lex[56]=1071;
lex[57]=1081;
lex[58]=1096;
lex[59]=1112;
lex[60]=1128;
lex[61]=1143;
lex[62]=1161;
lex[63]=1176;
lex[64]=1190;
lex[65]=1205;
lex[66]=1135;
lex[67]=1145;
lex[68]=1157;
lex[69]=1171;
lex[70]=1185;
lex[71]=1198;
lex[72]=1209;
lex[73]=1217;
lex[74]=1226;
lex[75]=1234;
lex[76]=1242;
lex[77]=1250;
lex[78]=1259;
lex[79]=1266;
lex[80]=1278;
lex[81]=1289;
lex[82]=1297;
lex[83]=1306;
lex[84]=1317;
lex[85]=1331;
lex[86]=1347;
lex[87]=1361;
lex[88]=1374;
lex[89]=1386;
lex[90]=1400;
lex[91]=1414;
lex[92]=1425;
lex[93]=1436;
lex[94]=1445;
lex[95]=1452;
lex[96]=1457;
lex[97]=1459;
lex[98]=1462;
lex[99]=1465;
lex[100]=1469;
lex[101]=1476;
lex[102]=1484;
lex[103]=1488;
lex[104]=1501;
lex[105]=1514;
lex[106]=1524;
lex[107]=1532;
lex[108]=1544;
lex[109]=1555;
lex[110]=1563;
lex[111]=1572;
lex[112]=1580;
lex[113]=1587;
lex[114]=1593;
lex[115]=1597;
lex[116]=1603;
lex[117]=1609;
lex[118]=1617;
lex[119]=1624;
lex[120]=1635;
lex[121]=1647;
lex[122]=1661;
lex[123]=1678;
lex[124]=1695;
lex[125]=1709;
lex[126]=1722;
lex[127]=1734;
lex[128]=1747;
lex[129]=1760;
lex[130]=1771;
lex[131]=1783;
lex[132]=1795;
lex[133]=1811;
lex[134]=1827;
lex[135]=1843;
lex[136]=1858;
lex[137]=1873;
lex[138]=1890;
lex[139]=1906;
lex[140]=1923;
lex[141]=1940;
lex[142]=1956;
lex[143]=1975;
lex[144]=1994;
lex[145]=2012;
lex[146]=2032;
lex[147]=2052;
lex[148]=2071;
lex[149]=2087;
lex[150]=2102;
lex[151]=2116;
lex[152]=2075;
lex[153]=2082;
lex[154]=2089;
lex[155]=2097;
lex[156]=2104;
lex[157]=2115;
lex[158]=2124;
lex[159]=2135;
lex[160]=2146;
lex[161]=2152;
lex[162]=2159;
lex[163]=2168;
lex[164]=2174;
lex[165]=2179;
lex[166]=2183;
lex[167]=2189;
lex[168]=2196;
lex[169]=2201;
lex[170]=2207;
lex[171]=2211;
lex[172]=2215;
lex[173]=2220;
lex[174]=2226;
lex[175]=2231;
lex[176]=2240;
lex[177]=2250;
lex[178]=2260;
lex[179]=2273;
lex[180]=2241;
lex[181]=2248;
lex[182]=2254;
lex[183]=2262;
lex[184]=2270;
lex[185]=2277;
lex[186]=2286;
lex[187]=2291;
lex[188]=2302;
lex[189]=2315;
lex[190]=2328;
lex[191]=2349;


ley[1]=172;
ley[2]=169;
ley[3]=166;
ley[4]=165;
ley[5]=163;
ley[6]=160;
ley[7]=158;
ley[8]=157;
ley[9]=157;
ley[10]=157;
ley[11]=155;
ley[12]=153;
ley[13]=150;
ley[14]=148;
ley[15]=146;
ley[16]=145;
ley[17]=143;
ley[18]=142;
ley[19]=141;
ley[20]=140;
ley[21]=137;
ley[22]=135;
ley[23]=134;
ley[24]=132;
ley[25]=131;
ley[26]=130;
ley[27]=129;
ley[28]=129;
ley[29]=129;
ley[30]=130;
ley[31]=131;
ley[32]=132;
ley[33]=134;
ley[34]=133;
ley[35]=133;
ley[36]=151;
ley[37]=151;
ley[38]=149;
ley[39]=148;
ley[40]=148;
ley[41]=146;
ley[42]=144;
ley[43]=142;
ley[44]=141;
ley[45]=139;
ley[46]=139;
ley[47]=139;
ley[48]=136;
ley[49]=135;
ley[50]=134;
ley[51]=134;
ley[52]=133;
ley[53]=134;
ley[54]=134;
ley[55]=133;
ley[56]=132;
ley[57]=132;
ley[58]=131;
ley[59]=130;
ley[60]=130;
ley[61]=128;
ley[62]=126;
ley[63]=126;
ley[64]=125;
ley[65]=123;
ley[66]=136;
ley[67]=135;
ley[68]=136;
ley[69]=135;
ley[70]=134;
ley[71]=133;
ley[72]=134;
ley[73]=134;
ley[74]=136;
ley[75]=136;
ley[76]=134;
ley[77]=136;
ley[78]=135;
ley[79]=134;
ley[80]=134;
ley[81]=134;
ley[82]=135;
ley[83]=137;
ley[84]=138;
ley[85]=138;
ley[86]=140;
ley[87]=141;
ley[88]=142;
ley[89]=141;
ley[90]=142;
ley[91]=143;
ley[92]=144;
ley[93]=145;
ley[94]=146;
ley[95]=145;
ley[96]=145;
ley[97]=145;
ley[98]=145;
ley[99]=146;
ley[100]=144;
ley[101]=145;
ley[102]=146;
ley[103]=147;
ley[104]=149;
ley[105]=151;
ley[106]=152;
ley[107]=152;
ley[108]=153;
ley[109]=153;
ley[110]=154;
ley[111]=155;
ley[112]=156;
ley[113]=156;
ley[114]=158;
ley[115]=158;
ley[116]=159;
ley[117]=160;
ley[118]=161;
ley[119]=161;
ley[120]=161;
ley[121]=162;
ley[122]=163;
ley[123]=163;
ley[124]=164;
ley[125]=163;
ley[126]=163;
ley[127]=164;
ley[128]=164;
ley[129]=166;
ley[130]=167;
ley[131]=168;
ley[132]=170;
ley[133]=174;
ley[134]=176;
ley[135]=176;
ley[136]=178;
ley[137]=179;
ley[138]=182;
ley[139]=183;
ley[140]=184;
ley[141]=187;
ley[142]=191;
ley[143]=195;
ley[144]=198;
ley[145]=202;
ley[146]=206;
ley[147]=209;
ley[148]=212;
ley[149]=215;
ley[150]=217;
ley[151]=220;
ley[152]=220;
ley[153]=222;
ley[154]=224;
ley[155]=226;
ley[156]=230;
ley[157]=232;
ley[158]=235;
ley[159]=239;
ley[160]=240;
ley[161]=242;
ley[162]=242;
ley[163]=245;
ley[164]=247;
ley[165]=247;
ley[166]=250;
ley[167]=252;
ley[168]=254;
ley[169]=255;
ley[170]=258;
ley[171]=262;
ley[172]=265;
ley[173]=267;
ley[174]=271;
ley[175]=272;
ley[176]=275;
ley[177]=278;
ley[178]=282;
ley[179]=287;
ley[180]=284;
ley[181]=287;
ley[182]=290;
ley[183]=293;
ley[184]=297;
ley[185]=301;
ley[186]=305;
ley[187]=310;
ley[188]=317;
ley[189]=325;
ley[190]=333;
ley[191]=344;


centerx[1]=258;
centerx[2]=263;
centerx[3]=268;
centerx[4]=272;
centerx[5]=277;
centerx[6]=283;
centerx[7]=288;
centerx[8]=293;
centerx[9]=298;
centerx[10]=303;
centerx[11]=309;
centerx[12]=314;
centerx[13]=320;
centerx[14]=325;
centerx[15]=330;
centerx[16]=337;
centerx[17]=342;
centerx[18]=349;
centerx[19]=356;
centerx[20]=361;
centerx[21]=367;
centerx[22]=374;
centerx[23]=382;
centerx[24]=388;
centerx[25]=395;
centerx[26]=403;
centerx[27]=409;
centerx[28]=417;
centerx[29]=425;
centerx[30]=435;
centerx[31]=442;
centerx[32]=452;
centerx[33]=462;
centerx[34]=471;
centerx[35]=481;
centerx[36]=535;
centerx[37]=546;
centerx[38]=557;
centerx[39]=566;
centerx[40]=576;
centerx[41]=586;
centerx[42]=595;
centerx[43]=605;
centerx[44]=613;
centerx[45]=623;
centerx[46]=634;
centerx[47]=644;
centerx[48]=653;
centerx[49]=664;
centerx[50]=674;
centerx[51]=685;
centerx[52]=695;
centerx[53]=704;
centerx[54]=714;
centerx[55]=724;
centerx[56]=733;
centerx[57]=740;
centerx[58]=750;
centerx[59]=760;
centerx[60]=770;
centerx[61]=781;
centerx[62]=792;
centerx[63]=802;
centerx[64]=811;
centerx[65]=820;
centerx[66]=927;
centerx[67]=938;
centerx[68]=948;
centerx[69]=960;
centerx[70]=972;
centerx[71]=984;
centerx[72]=996;
centerx[73]=1005;
centerx[74]=1017;
centerx[75]=1027;
centerx[76]=1036;
centerx[77]=1046;
centerx[78]=1057;
centerx[79]=1066;
centerx[80]=1076;
centerx[81]=1085;
centerx[82]=1097;
centerx[83]=1108;
centerx[84]=1119;
centerx[85]=1130;
centerx[86]=1142;
centerx[87]=1152;
centerx[88]=1163;
centerx[89]=1174;
centerx[90]=1185;
centerx[91]=1197;
centerx[92]=1207;
centerx[93]=1218;
centerx[94]=1227;
centerx[95]=1237;
centerx[96]=1244;
centerx[97]=1252;
centerx[98]=1260;
centerx[99]=1267;
centerx[100]=1275;
centerx[101]=1283;
centerx[102]=1291;
centerx[103]=1298;
centerx[104]=1308;
centerx[105]=1317;
centerx[106]=1327;
centerx[107]=1336;
centerx[108]=1345;
centerx[109]=1356;
centerx[110]=1364;
centerx[111]=1372;
centerx[112]=1379;
centerx[113]=1387;
centerx[114]=1395;
centerx[115]=1402;
centerx[116]=1410;
centerx[117]=1418;
centerx[118]=1427;
centerx[119]=1435;
centerx[120]=1444;
centerx[121]=1452;
centerx[122]=1461;
centerx[123]=1472;
centerx[124]=1483;
centerx[125]=1493;
centerx[126]=1503;
centerx[127]=1513;
centerx[128]=1523;
centerx[129]=1535;
centerx[130]=1546;
centerx[131]=1554;
centerx[132]=1564;
centerx[133]=1574;
centerx[134]=1587;
centerx[135]=1600;
centerx[136]=1612;
centerx[137]=1624;
centerx[138]=1636;
centerx[139]=1646;
centerx[140]=1658;
centerx[141]=1671;
centerx[142]=1682;
centerx[143]=1695;
centerx[144]=1707;
centerx[145]=1720;
centerx[146]=1734;
centerx[147]=1750;
centerx[148]=1765;
centerx[149]=1779;
centerx[150]=1792;
centerx[151]=1807;
centerx[152]=1852;
centerx[153]=1864;
centerx[154]=1875;
centerx[155]=1886;
centerx[156]=1896;
centerx[157]=1907;
centerx[158]=1919;
centerx[159]=1930;
centerx[160]=1939;
centerx[161]=1948;
centerx[162]=1958;
centerx[163]=1969;
centerx[164]=1979;
centerx[165]=1989;
centerx[166]=1998;
centerx[167]=2006;
centerx[168]=2013;
centerx[169]=2020;
centerx[170]=2029;
centerx[171]=2035;
centerx[172]=2041;
centerx[173]=2047;
centerx[174]=2054;
centerx[175]=2059;
centerx[176]=2066;
centerx[177]=2073;
centerx[178]=2079;
centerx[179]=2087;
centerx[180]=2121;
centerx[181]=2128;
centerx[182]=2134;
centerx[183]=2142;
centerx[184]=2149;
centerx[185]=2156;
centerx[186]=2163;
centerx[187]=2166;
centerx[188]=2175;
centerx[189]=2184;
centerx[190]=2194;
centerx[191]=2207;


centery[1]=234;
centery[2]=232;
centery[3]=229;
centery[4]=228;
centery[5]=226;
centery[6]=225;
centery[7]=223;
centery[8]=221;
centery[9]=220;
centery[10]=219;
centery[11]=217;
centery[12]=216;
centery[13]=213;
centery[14]=212;
centery[15]=210;
centery[16]=208;
centery[17]=207;
centery[18]=205;
centery[19]=203;
centery[20]=202;
centery[21]=200;
centery[22]=198;
centery[23]=197;
centery[24]=196;
centery[25]=195;
centery[26]=194;
centery[27]=193;
centery[28]=192;
centery[29]=191;
centery[30]=190;
centery[31]=190;
centery[32]=189;
centery[33]=188;
centery[34]=186;
centery[35]=186;
centery[36]=175;
centery[37]=174;
centery[38]=173;
centery[39]=172;
centery[40]=171;
centery[41]=170;
centery[42]=170;
centery[43]=168;
centery[44]=167;
centery[45]=166;
centery[46]=165;
centery[47]=165;
centery[48]=163;
centery[49]=162;
centery[50]=161;
centery[51]=160;
centery[52]=160;
centery[53]=159;
centery[54]=159;
centery[55]=158;
centery[56]=157;
centery[57]=156;
centery[58]=155;
centery[59]=155;
centery[60]=154;
centery[61]=153;
centery[62]=152;
centery[63]=151;
centery[64]=151;
centery[65]=150;
centery[66]=142;
centery[67]=141;
centery[68]=141;
centery[69]=140;
centery[70]=140;
centery[71]=139;
centery[72]=139;
centery[73]=138;
centery[74]=138;
centery[75]=138;
centery[76]=137;
centery[77]=137;
centery[78]=137;
centery[79]=137;
centery[80]=137;
centery[81]=137;
centery[82]=137;
centery[83]=138;
centery[84]=138;
centery[85]=138;
centery[86]=138;
centery[87]=137;
centery[88]=138;
centery[89]=137;
centery[90]=138;
centery[91]=138;
centery[92]=138;
centery[93]=138;
centery[94]=139;
centery[95]=139;
centery[96]=139;
centery[97]=139;
centery[98]=139;
centery[99]=140;
centery[100]=140;
centery[101]=140;
centery[102]=141;
centery[103]=141;
centery[104]=141;
centery[105]=142;
centery[106]=142;
centery[107]=142;
centery[108]=142;
centery[109]=142;
centery[110]=143;
centery[111]=144;
centery[112]=145;
centery[113]=145;
centery[114]=145;
centery[115]=145;
centery[116]=145;
centery[117]=146;
centery[118]=146;
centery[119]=147;
centery[120]=147;
centery[121]=148;
centery[122]=149;
centery[123]=149;
centery[124]=150;
centery[125]=151;
centery[126]=151;
centery[127]=151;
centery[128]=152;
centery[129]=152;
centery[130]=153;
centery[131]=154;
centery[132]=155;
centery[133]=157;
centery[134]=157;
centery[135]=158;
centery[136]=159;
centery[137]=160;
centery[138]=161;
centery[139]=162;
centery[140]=162;
centery[141]=164;
centery[142]=166;
centery[143]=167;
centery[144]=169;
centery[145]=170;
centery[146]=172;
centery[147]=174;
centery[148]=176;
centery[149]=177;
centery[150]=179;
centery[151]=180;
centery[152]=186;
centery[153]=188;
centery[154]=191;
centery[155]=192;
centery[156]=194;
centery[157]=196;
centery[158]=198;
centery[159]=200;
centery[160]=202;
centery[161]=204;
centery[162]=205;
centery[163]=207;
centery[164]=210;
centery[165]=211;
centery[166]=213;
centery[167]=215;
centery[168]=216;
centery[169]=218;
centery[170]=220;
centery[171]=222;
centery[172]=224;
centery[173]=226;
centery[174]=229;
centery[175]=230;
centery[176]=232;
centery[177]=234;
centery[178]=236;
centery[179]=238;
centery[180]=247;
centery[181]=249;
centery[182]=251;
centery[183]=254;
centery[184]=257;
centery[185]=260;
centery[186]=262;
centery[187]=265;
centery[188]=269;
centery[189]=273;
centery[190]=277;
centery[191]=283;

		

	}

}
