package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class aaImageTestAug11 implements PlugIn {

	int[] centerx;
	int[] centery;
	//int[] centerZ;
	int[] lex;
	int[] ley;
	//int[] leZ;

	public void run(String arg) {
		int width=2816;
		int height=350;
		int frames = 166;
		
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
lex[1]=374;
lex[2]=385;
lex[3]=395;
lex[4]=405;
lex[5]=419;
lex[6]=434;
lex[7]=443;
lex[8]=450;
lex[9]=461;
lex[10]=477;
lex[11]=493;
lex[12]=504;
lex[13]=516;
lex[14]=521;
lex[15]=533;
lex[16]=549;
lex[17]=560;
lex[18]=569;
lex[19]=581;
lex[20]=593;
lex[21]=603;
lex[22]=612;
lex[23]=621;
lex[24]=625;
lex[25]=633;
lex[26]=641;
lex[27]=660;
lex[28]=674;
lex[29]=691;
lex[30]=709;
lex[31]=723;
lex[32]=738;
lex[33]=749;
lex[34]=766;
lex[35]=784;
lex[36]=803;
lex[37]=821;
lex[38]=840;
lex[39]=858;
lex[40]=875;
lex[41]=890;
lex[42]=905;
lex[43]=923;
lex[44]=943;
lex[45]=961;
lex[46]=977;
lex[47]=989;
lex[48]=1001;
lex[49]=1017;
lex[50]=1030;
lex[51]=1043;
lex[52]=1061;
lex[53]=1075;
lex[54]=1089;
lex[55]=1102;
lex[56]=1118;
lex[57]=1133;
lex[58]=1149;
lex[59]=1162;
lex[60]=1176;
lex[61]=1193;
lex[62]=1211;
lex[63]=1228;
lex[64]=1245;
lex[65]=1262;
lex[66]=1276;
lex[67]=1291;
lex[68]=1306;
lex[69]=1321;
lex[70]=1336;
lex[71]=1352;
lex[72]=1365;
lex[73]=1379;
lex[74]=1394;
lex[75]=1411;
lex[76]=1426;
lex[77]=1444;
lex[78]=1458;
lex[79]=1476;
lex[80]=1492;
lex[81]=1506;
lex[82]=1520;
lex[83]=1536;
lex[84]=1554;
lex[85]=1571;
lex[86]=1584;
lex[87]=1601;
lex[88]=1619;
lex[89]=1637;
lex[90]=1652;
lex[91]=1672;
lex[92]=1691;
lex[93]=1710;
lex[94]=1732;
lex[95]=1748;
lex[96]=1768;
lex[97]=1786;
lex[98]=1801;
lex[99]=1814;
lex[100]=1829;
lex[101]=1847;
lex[102]=1865;
lex[103]=1888;
lex[104]=1908;
lex[105]=1926;
lex[106]=1945;
lex[107]=1965;
lex[108]=1983;
lex[109]=1998;
lex[110]=1947;
lex[111]=1963;
lex[112]=1980;
lex[113]=1997;
lex[114]=2017;
lex[115]=2039;
lex[116]=2058;
lex[117]=2075;
lex[118]=2094;
lex[119]=2112;
lex[120]=2128;
lex[121]=2141;
lex[122]=2154;
lex[123]=2171;
lex[124]=2187;
lex[125]=2200;
lex[126]=2213;
lex[127]=2227;
lex[128]=2241;
lex[129]=2256;
lex[130]=2272;
lex[131]=2291;
lex[132]=2308;
lex[133]=2325;
lex[134]=2343;
lex[135]=2360;
lex[136]=2379;
lex[137]=2398;
lex[138]=2418;
lex[139]=2438;
lex[140]=2461;
lex[141]=2481;
lex[142]=2501;
lex[143]=2526;
lex[144]=2496;
lex[145]=2511;
lex[146]=2526;
lex[147]=2542;
lex[148]=2556;
lex[149]=2573;
lex[150]=2585;
lex[151]=2599;
lex[152]=2613;
lex[153]=2626;
lex[154]=2640;
lex[155]=2653;
lex[156]=2665;
lex[157]=2680;
lex[158]=2695;
lex[159]=2708;
lex[160]=2722;
lex[161]=2736;
lex[162]=2749;
lex[163]=2764;
lex[164]=2775;
lex[165]=2786;
lex[166]=2798;



ley[1]=169;
ley[2]=164;
ley[3]=161;
ley[4]=158;
ley[5]=152;
ley[6]=146;
ley[7]=143;
ley[8]=142;
ley[9]=137;
ley[10]=132;
ley[11]=124;
ley[12]=124;
ley[13]=123;
ley[14]=123;
ley[15]=118;
ley[16]=116;
ley[17]=115;
ley[18]=115;
ley[19]=111;
ley[20]=110;
ley[21]=109;
ley[22]=109;
ley[23]=112;
ley[24]=113;
ley[25]=112;
ley[26]=111;
ley[27]=107;
ley[28]=105;
ley[29]=103;
ley[30]=102;
ley[31]=102;
ley[32]=100;
ley[33]=101;
ley[34]=101;
ley[35]=103;
ley[36]=107;
ley[37]=111;
ley[38]=112;
ley[39]=115;
ley[40]=116;
ley[41]=118;
ley[42]=118;
ley[43]=118;
ley[44]=118;
ley[45]=118;
ley[46]=119;
ley[47]=120;
ley[48]=120;
ley[49]=120;
ley[50]=121;
ley[51]=123;
ley[52]=125;
ley[53]=126;
ley[54]=128;
ley[55]=129;
ley[56]=118;
ley[57]=118;
ley[58]=118;
ley[59]=120;
ley[60]=133;
ley[61]=131;
ley[62]=129;
ley[63]=129;
ley[64]=130;
ley[65]=131;
ley[66]=132;
ley[67]=134;
ley[68]=134;
ley[69]=122;
ley[70]=122;
ley[71]=138;
ley[72]=137;
ley[73]=136;
ley[74]=127;
ley[75]=136;
ley[76]=135;
ley[77]=136;
ley[78]=138;
ley[79]=129;
ley[80]=130;
ley[81]=129;
ley[82]=127;
ley[83]=127;
ley[84]=127;
ley[85]=127;
ley[86]=126;
ley[87]=126;
ley[88]=159;
ley[89]=162;
ley[90]=126;
ley[91]=127;
ley[92]=128;
ley[93]=166;
ley[94]=166;
ley[95]=163;
ley[96]=158;
ley[97]=155;
ley[98]=155;
ley[99]=143;
ley[100]=145;
ley[101]=147;
ley[102]=147;
ley[103]=148;
ley[104]=149;
ley[105]=148;
ley[106]=149;
ley[107]=148;
ley[108]=147;
ley[109]=146;
ley[110]=143;
ley[111]=144;
ley[112]=143;
ley[113]=143;
ley[114]=143;
ley[115]=143;
ley[116]=146;
ley[117]=148;
ley[118]=149;
ley[119]=150;
ley[120]=159;
ley[121]=152;
ley[122]=150;
ley[123]=148;
ley[124]=148;
ley[125]=148;
ley[126]=148;
ley[127]=148;
ley[128]=150;
ley[129]=150;
ley[130]=151;
ley[131]=153;
ley[132]=154;
ley[133]=156;
ley[134]=158;
ley[135]=160;
ley[136]=160;
ley[137]=159;
ley[138]=158;
ley[139]=160;
ley[140]=162;
ley[141]=164;
ley[142]=166;
ley[143]=168;
ley[144]=231;
ley[145]=234;
ley[146]=197;
ley[147]=238;
ley[148]=204;
ley[149]=206;
ley[150]=209;
ley[151]=213;
ley[152]=216;
ley[153]=218;
ley[154]=221;
ley[155]=223;
ley[156]=255;
ley[157]=257;
ley[158]=259;
ley[159]=232;
ley[160]=235;
ley[161]=262;
ley[162]=237;
ley[163]=238;
ley[164]=238;
ley[165]=239;
ley[166]=240;



centerx[1]=238;
centerx[2]=245;
centerx[3]=252;
centerx[4]=261;
centerx[5]=269;
centerx[6]=277;
centerx[7]=285;
centerx[8]=292;
centerx[9]=300;
centerx[10]=309;
centerx[11]=321;
centerx[12]=329;
centerx[13]=344;
centerx[14]=359;
centerx[15]=371;
centerx[16]=386;
centerx[17]=395;
centerx[18]=402;
centerx[19]=413;
centerx[20]=424;
centerx[21]=437;
centerx[22]=451;
centerx[23]=463;
centerx[24]=478;
centerx[25]=492;
centerx[26]=506;
centerx[27]=522;
centerx[28]=534;
centerx[29]=544;
centerx[30]=555;
centerx[31]=565;
centerx[32]=574;
centerx[33]=581;
centerx[34]=593;
centerx[35]=604;
centerx[36]=618;
centerx[37]=632;
centerx[38]=646;
centerx[39]=658;
centerx[40]=674;
centerx[41]=689;
centerx[42]=705;
centerx[43]=722;
centerx[44]=740;
centerx[45]=760;
centerx[46]=777;
centerx[47]=798;
centerx[48]=816;
centerx[49]=836;
centerx[50]=853;
centerx[51]=869;
centerx[52]=885;
centerx[53]=901;
centerx[54]=917;
centerx[55]=932;
centerx[56]=946;
centerx[57]=961;
centerx[58]=976;
centerx[59]=990;
centerx[60]=1005;
centerx[61]=1020;
centerx[62]=1036;
centerx[63]=1051;
centerx[64]=1068;
centerx[65]=1084;
centerx[66]=1099;
centerx[67]=1112;
centerx[68]=1127;
centerx[69]=1143;
centerx[70]=1159;
centerx[71]=1175;
centerx[72]=1191;
centerx[73]=1207;
centerx[74]=1221;
centerx[75]=1239;
centerx[76]=1254;
centerx[77]=1270;
centerx[78]=1283;
centerx[79]=1298;
centerx[80]=1313;
centerx[81]=1327;
centerx[82]=1341;
centerx[83]=1356;
centerx[84]=1373;
centerx[85]=1390;
centerx[86]=1406;
centerx[87]=1421;
centerx[88]=1437;
centerx[89]=1451;
centerx[90]=1465;
centerx[91]=1482;
centerx[92]=1498;
centerx[93]=1514;
centerx[94]=1534;
centerx[95]=1550;
centerx[96]=1567;
centerx[97]=1581;
centerx[98]=1596;
centerx[99]=1613;
centerx[100]=1630;
centerx[101]=1646;
centerx[102]=1661;
centerx[103]=1676;
centerx[104]=1691;
centerx[105]=1707;
centerx[106]=1724;
centerx[107]=1740;
centerx[108]=1754;
centerx[109]=1769;
centerx[110]=1819;
centerx[111]=1835;
centerx[112]=1851;
centerx[113]=1868;
centerx[114]=1883;
centerx[115]=1900;
centerx[116]=1919;
centerx[117]=1936;
centerx[118]=1952;
centerx[119]=1969;
centerx[120]=1984;
centerx[121]=1999;
centerx[122]=2015;
centerx[123]=2030;
centerx[124]=2044;
centerx[125]=2056;
centerx[126]=2071;
centerx[127]=2085;
centerx[128]=2098;
centerx[129]=2114;
centerx[130]=2127;
centerx[131]=2144;
centerx[132]=2160;
centerx[133]=2176;
centerx[134]=2192;
centerx[135]=2208;
centerx[136]=2223;
centerx[137]=2236;
centerx[138]=2251;
centerx[139]=2267;
centerx[140]=2285;
centerx[141]=2302;
centerx[142]=2317;
centerx[143]=2333;
centerx[144]=2376;
centerx[145]=2393;
centerx[146]=2410;
centerx[147]=2426;
centerx[148]=2441;
centerx[149]=2456;
centerx[150]=2469;
centerx[151]=2484;
centerx[152]=2498;
centerx[153]=2511;
centerx[154]=2524;
centerx[155]=2538;
centerx[156]=2551;
centerx[157]=2565;
centerx[158]=2579;
centerx[159]=2592;
centerx[160]=2605;
centerx[161]=2618;
centerx[162]=2632;
centerx[163]=2646;
centerx[164]=2657;
centerx[165]=2668;
centerx[166]=2679;



centery[1]=234;
centery[2]=232;
centery[3]=229;
centery[4]=226;
centery[5]=223;
centery[6]=219;
centery[7]=216;
centery[8]=213;
centery[9]=210;
centery[10]=207;
centery[11]=201;
centery[12]=200;
centery[13]=195;
centery[14]=188;
centery[15]=183;
centery[16]=178;
centery[17]=175;
centery[18]=172;
centery[19]=167;
centery[20]=164;
centery[21]=160;
centery[22]=156;
centery[23]=152;
centery[24]=148;
centery[25]=144;
centery[26]=140;
centery[27]=137;
centery[28]=133;
centery[29]=131;
centery[30]=130;
centery[31]=128;
centery[32]=125;
centery[33]=123;
centery[34]=122;
centery[35]=122;
centery[36]=122;
centery[37]=122;
centery[38]=122;
centery[39]=121;
centery[40]=120;
centery[41]=120;
centery[42]=120;
centery[43]=120;
centery[44]=120;
centery[45]=120;
centery[46]=120;
centery[47]=119;
centery[48]=119;
centery[49]=119;
centery[50]=120;
centery[51]=121;
centery[52]=122;
centery[53]=122;
centery[54]=123;
centery[55]=124;
centery[56]=126;
centery[57]=126;
centery[58]=126;
centery[59]=127;
centery[60]=127;
centery[61]=127;
centery[62]=126;
centery[63]=127;
centery[64]=127;
centery[65]=128;
centery[66]=128;
centery[67]=128;
centery[68]=128;
centery[69]=128;
centery[70]=129;
centery[71]=130;
centery[72]=131;
centery[73]=131;
centery[74]=132;
centery[75]=132;
centery[76]=132;
centery[77]=133;
centery[78]=134;
centery[79]=134;
centery[80]=134;
centery[81]=135;
centery[82]=136;
centery[83]=137;
centery[84]=137;
centery[85]=138;
centery[86]=140;
centery[87]=141;
centery[88]=142;
centery[89]=144;
centery[90]=144;
centery[91]=146;
centery[92]=147;
centery[93]=148;
centery[94]=149;
centery[95]=149;
centery[96]=148;
centery[97]=148;
centery[98]=148;
centery[99]=148;
centery[100]=148;
centery[101]=148;
centery[102]=147;
centery[103]=147;
centery[104]=147;
centery[105]=147;
centery[106]=145;
centery[107]=145;
centery[108]=145;
centery[109]=146;
centery[110]=146;
centery[111]=148;
centery[112]=149;
centery[113]=150;
centery[114]=150;
centery[115]=152;
centery[116]=152;
centery[117]=152;
centery[118]=153;
centery[119]=154;
centery[120]=155;
centery[121]=155;
centery[122]=156;
centery[123]=157;
centery[124]=158;
centery[125]=159;
centery[126]=160;
centery[127]=161;
centery[128]=163;
centery[129]=165;
centery[130]=166;
centery[131]=169;
centery[132]=171;
centery[133]=172;
centery[134]=174;
centery[135]=176;
centery[136]=177;
centery[137]=180;
centery[138]=183;
centery[139]=186;
centery[140]=190;
centery[141]=194;
centery[142]=197;
centery[143]=201;
centery[144]=209;
centery[145]=213;
centery[146]=216;
centery[147]=219;
centery[148]=222;
centery[149]=226;
centery[150]=228;
centery[151]=230;
centery[152]=233;
centery[153]=234;
centery[154]=236;
centery[155]=238;
centery[156]=240;
centery[157]=242;
centery[158]=245;
centery[159]=246;
centery[160]=248;
centery[161]=249;
centery[162]=251;
centery[163]=253;
centery[164]=254;
centery[165]=257;
centery[166]=260;


	}

}
