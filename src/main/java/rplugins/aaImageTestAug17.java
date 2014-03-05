package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class aaImageTestAug17 implements PlugIn {

	int[] centerx;
	int[] centery;
	//int[] centerZ;
	int[] lex;
	int[] ley;
	//int[] leZ;

	public void run(String arg) {
		int width=2816;
		int height=304;
		int frames = 181;
		
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

		
lex[1]=414;
lex[2]=414;
lex[3]=410;
lex[4]=412;
lex[5]=417;
lex[6]=428;
lex[7]=439;
lex[8]=451;
lex[9]=461;
lex[10]=476;
lex[11]=492;
lex[12]=507;
lex[13]=525;
lex[14]=545;
lex[15]=567;
lex[16]=578;
lex[17]=592;
lex[18]=604;
lex[19]=619;
lex[20]=634;
lex[21]=648;
lex[22]=664;
lex[23]=678;
lex[24]=693;
lex[25]=712;
lex[26]=730;
lex[27]=750;
lex[28]=772;
lex[29]=791;
lex[30]=814;
lex[31]=832;
lex[32]=848;
lex[33]=863;
lex[34]=881;
lex[35]=893;
lex[36]=905;
lex[37]=910;
lex[38]=924;
lex[39]=936;
lex[40]=951;
lex[41]=967;
lex[42]=916;
lex[43]=926;
lex[44]=936;
lex[45]=947;
lex[46]=961;
lex[47]=972;
lex[48]=985;
lex[49]=1003;
lex[50]=1019;
lex[51]=1032;
lex[52]=1051;
lex[53]=1065;
lex[54]=1075;
lex[55]=1090;
lex[56]=1110;
lex[57]=1121;
lex[58]=1136;
lex[59]=1147;
lex[60]=1165;
lex[61]=1180;
lex[62]=1189;
lex[63]=1203;
lex[64]=1218;
lex[65]=1232;
lex[66]=1237;
lex[67]=1250;
lex[68]=1272;
lex[69]=1283;
lex[70]=1292;
lex[71]=1301;
lex[72]=1310;
lex[73]=1322;
lex[74]=1330;
lex[75]=1342;
lex[76]=1357;
lex[77]=1372;
lex[78]=1384;
lex[79]=1397;
lex[80]=1415;
lex[81]=1424;
lex[82]=1437;
lex[83]=1449;
lex[84]=1463;
lex[85]=1478;
lex[86]=1495;
lex[87]=1508;
lex[88]=1520;
lex[89]=1532;
lex[90]=1545;
lex[91]=1559;
lex[92]=1576;
lex[93]=1592;
lex[94]=1612;
lex[95]=1629;
lex[96]=1648;
lex[97]=1666;
lex[98]=1681;
lex[99]=1694;
lex[100]=1704;
lex[101]=1711;
lex[102]=1722;
lex[103]=1743;
lex[104]=1757;
lex[105]=1764;
lex[106]=1786;
lex[107]=1745;
lex[108]=1753;
lex[109]=1759;
lex[110]=1776;
lex[111]=1835;
lex[112]=1849;
lex[113]=1868;
lex[114]=1890;
lex[115]=1911;
lex[116]=1879;
lex[117]=1907;
lex[118]=1934;
lex[119]=1957;
lex[120]=1974;
lex[121]=1986;
lex[122]=2005;
lex[123]=2027;
lex[124]=2032;
lex[125]=2040;
lex[126]=2064;
lex[127]=2077;
lex[128]=2096;
lex[129]=2114;
lex[130]=2134;
lex[131]=2151;
lex[132]=2169;
lex[133]=2182;
lex[134]=2199;
lex[135]=2211;
lex[136]=2219;
lex[137]=2231;
lex[138]=2243;
lex[139]=2253;
lex[140]=2262;
lex[141]=2271;
lex[142]=2282;
lex[143]=2291;
lex[144]=2300;
lex[145]=2310;
lex[146]=2320;
lex[147]=2337;
lex[148]=2350;
lex[149]=2364;
lex[150]=2377;
lex[151]=2390;
lex[152]=2407;
lex[153]=2425;
lex[154]=2441;
lex[155]=2455;
lex[156]=2469;
lex[157]=2448;
lex[158]=2457;
lex[159]=2466;
lex[160]=2474;
lex[161]=2484;
lex[162]=2493;
lex[163]=2505;
lex[164]=2515;
lex[165]=2523;
lex[166]=2531;
lex[167]=2537;
lex[168]=2546;
lex[169]=2552;
lex[170]=2559;
lex[171]=2567;
lex[172]=2577;
lex[173]=2584;
lex[174]=2595;
lex[175]=2606;
lex[176]=2615;
lex[177]=2628;
lex[178]=2638;
lex[179]=2646;
lex[180]=2657;
lex[181]=2666;



ley[1]=167;
ley[2]=165;
ley[3]=169;
ley[4]=168;
ley[5]=164;
ley[6]=160;
ley[7]=152;
ley[8]=145;
ley[9]=141;
ley[10]=130;
ley[11]=125;
ley[12]=116;
ley[13]=110;
ley[14]=102;
ley[15]=95;
ley[16]=96;
ley[17]=88;
ley[18]=90;
ley[19]=87;
ley[20]=85;
ley[21]=86;
ley[22]=80;
ley[23]=78;
ley[24]=77;
ley[25]=75;
ley[26]=73;
ley[27]=68;
ley[28]=69;
ley[29]=68;
ley[30]=66;
ley[31]=64;
ley[32]=65;
ley[33]=67;
ley[34]=69;
ley[35]=72;
ley[36]=74;
ley[37]=81;
ley[38]=79;
ley[39]=87;
ley[40]=82;
ley[41]=84;
ley[42]=107;
ley[43]=108;
ley[44]=111;
ley[45]=111;
ley[46]=114;
ley[47]=114;
ley[48]=112;
ley[49]=111;
ley[50]=109;
ley[51]=104;
ley[52]=102;
ley[53]=100;
ley[54]=102;
ley[55]=103;
ley[56]=97;
ley[57]=100;
ley[58]=104;
ley[59]=109;
ley[60]=105;
ley[61]=106;
ley[62]=105;
ley[63]=105;
ley[64]=100;
ley[65]=100;
ley[66]=107;
ley[67]=107;
ley[68]=102;
ley[69]=101;
ley[70]=103;
ley[71]=106;
ley[72]=107;
ley[73]=110;
ley[74]=106;
ley[75]=105;
ley[76]=107;
ley[77]=107;
ley[78]=108;
ley[79]=107;
ley[80]=105;
ley[81]=104;
ley[82]=102;
ley[83]=103;
ley[84]=107;
ley[85]=108;
ley[86]=109;
ley[87]=112;
ley[88]=115;
ley[89]=113;
ley[90]=115;
ley[91]=114;
ley[92]=116;
ley[93]=114;
ley[94]=108;
ley[95]=105;
ley[96]=103;
ley[97]=101;
ley[98]=103;
ley[99]=103;
ley[100]=103;
ley[101]=105;
ley[102]=110;
ley[103]=106;
ley[104]=109;
ley[105]=106;
ley[106]=108;
ley[107]=105;
ley[108]=109;
ley[109]=106;
ley[110]=107;
ley[111]=104;
ley[112]=103;
ley[113]=104;
ley[114]=103;
ley[115]=107;
ley[116]=114;
ley[117]=113;
ley[118]=116;
ley[119]=113;
ley[120]=114;
ley[121]=109;
ley[122]=110;
ley[123]=134;
ley[124]=145;
ley[125]=145;
ley[126]=141;
ley[127]=145;
ley[128]=146;
ley[129]=148;
ley[130]=148;
ley[131]=150;
ley[132]=153;
ley[133]=156;
ley[134]=158;
ley[135]=125;
ley[136]=128;
ley[137]=129;
ley[138]=131;
ley[139]=132;
ley[140]=137;
ley[141]=138;
ley[142]=141;
ley[143]=143;
ley[144]=144;
ley[145]=146;
ley[146]=148;
ley[147]=149;
ley[148]=151;
ley[149]=152;
ley[150]=153;
ley[151]=154;
ley[152]=156;
ley[153]=156;
ley[154]=155;
ley[155]=153;
ley[156]=155;
ley[157]=162;
ley[158]=164;
ley[159]=165;
ley[160]=165;
ley[161]=167;
ley[162]=167;
ley[163]=168;
ley[164]=169;
ley[165]=171;
ley[166]=172;
ley[167]=176;
ley[168]=179;
ley[169]=181;
ley[170]=184;
ley[171]=185;
ley[172]=186;
ley[173]=186;
ley[174]=190;
ley[175]=189;
ley[176]=252;
ley[177]=188;
ley[178]=188;
ley[179]=190;
ley[180]=191;
ley[181]=193;



centerx[1]=268;
centerx[2]=269;
centerx[3]=270;
centerx[4]=269;
centerx[5]=275;
centerx[6]=280;
centerx[7]=284;
centerx[8]=291;
centerx[9]=296;
centerx[10]=303;
centerx[11]=310;
centerx[12]=318;
centerx[13]=327;
centerx[14]=337;
centerx[15]=349;
centerx[16]=359;
centerx[17]=369;
centerx[18]=378;
centerx[19]=387;
centerx[20]=396;
centerx[21]=405;
centerx[22]=414;
centerx[23]=424;
centerx[24]=434;
centerx[25]=446;
centerx[26]=458;
centerx[27]=471;
centerx[28]=483;
centerx[29]=498;
centerx[30]=509;
centerx[31]=520;
centerx[32]=528;
centerx[33]=542;
centerx[34]=557;
centerx[35]=568;
centerx[36]=583;
centerx[37]=603;
centerx[38]=617;
centerx[39]=631;
centerx[40]=645;
centerx[41]=660;
centerx[42]=730;
centerx[43]=743;
centerx[44]=755;
centerx[45]=768;
centerx[46]=781;
centerx[47]=793;
centerx[48]=805;
centerx[49]=818;
centerx[50]=830;
centerx[51]=843;
centerx[52]=859;
centerx[53]=871;
centerx[54]=883;
centerx[55]=900;
centerx[56]=917;
centerx[57]=927;
centerx[58]=942;
centerx[59]=954;
centerx[60]=968;
centerx[61]=983;
centerx[62]=995;
centerx[63]=1007;
centerx[64]=1022;
centerx[65]=1037;
centerx[66]=1049;
centerx[67]=1063;
centerx[68]=1080;
centerx[69]=1090;
centerx[70]=1099;
centerx[71]=1109;
centerx[72]=1119;
centerx[73]=1132;
centerx[74]=1142;
centerx[75]=1153;
centerx[76]=1165;
centerx[77]=1177;
centerx[78]=1189;
centerx[79]=1201;
centerx[80]=1217;
centerx[81]=1226;
centerx[82]=1238;
centerx[83]=1248;
centerx[84]=1261;
centerx[85]=1277;
centerx[86]=1290;
centerx[87]=1301;
centerx[88]=1310;
centerx[89]=1323;
centerx[90]=1338;
centerx[91]=1352;
centerx[92]=1367;
centerx[93]=1380;
centerx[94]=1394;
centerx[95]=1409;
centerx[96]=1422;
centerx[97]=1435;
centerx[98]=1449;
centerx[99]=1463;
centerx[100]=1475;
centerx[101]=1487;
centerx[102]=1501;
centerx[103]=1518;
centerx[104]=1534;
centerx[105]=1541;
centerx[106]=1559;
centerx[107]=1606;
centerx[108]=1618;
centerx[109]=1622;
centerx[110]=1636;
centerx[111]=1607;
centerx[112]=1617;
centerx[113]=1631;
centerx[114]=1647;
centerx[115]=1660;
centerx[116]=1711;
centerx[117]=1734;
centerx[118]=1756;
centerx[119]=1776;
centerx[120]=1793;
centerx[121]=1809;
centerx[122]=1828;
centerx[123]=1851;
centerx[124]=1863;
centerx[125]=1877;
centerx[126]=1893;
centerx[127]=1906;
centerx[128]=1923;
centerx[129]=1937;
centerx[130]=1953;
centerx[131]=1968;
centerx[132]=1986;
centerx[133]=2000;
centerx[134]=2014;
centerx[135]=2026;
centerx[136]=2039;
centerx[137]=2055;
centerx[138]=2069;
centerx[139]=2082;
centerx[140]=2096;
centerx[141]=2105;
centerx[142]=2118;
centerx[143]=2127;
centerx[144]=2139;
centerx[145]=2151;
centerx[146]=2161;
centerx[147]=2174;
centerx[148]=2185;
centerx[149]=2196;
centerx[150]=2208;
centerx[151]=2221;
centerx[152]=2237;
centerx[153]=2248;
centerx[154]=2260;
centerx[155]=2273;
centerx[156]=2286;
centerx[157]=2319;
centerx[158]=2329;
centerx[159]=2338;
centerx[160]=2348;
centerx[161]=2360;
centerx[162]=2369;
centerx[163]=2379;
centerx[164]=2389;
centerx[165]=2397;
centerx[166]=2404;
centerx[167]=2414;
centerx[168]=2423;
centerx[169]=2430;
centerx[170]=2438;
centerx[171]=2444;
centerx[172]=2451;
centerx[173]=2457;
centerx[174]=2466;
centerx[175]=2473;
centerx[176]=2481;
centerx[177]=2490;
centerx[178]=2499;
centerx[179]=2506;
centerx[180]=2515;
centerx[181]=2523;



centery[1]=232;
centery[2]=230;
centery[3]=228;
centery[4]=230;
centery[5]=226;
centery[6]=224;
centery[7]=223;
centery[8]=217;
centery[9]=215;
centery[10]=210;
centery[11]=210;
centery[12]=203;
centery[13]=200;
centery[14]=194;
centery[15]=189;
centery[16]=188;
centery[17]=179;
centery[18]=179;
centery[19]=176;
centery[20]=172;
centery[21]=171;
centery[22]=167;
centery[23]=164;
centery[24]=162;
centery[25]=160;
centery[26]=157;
centery[27]=150;
centery[28]=149;
centery[29]=145;
centery[30]=144;
centery[31]=140;
centery[32]=140;
centery[33]=137;
centery[34]=135;
centery[35]=134;
centery[36]=132;
centery[37]=128;
centery[38]=125;
centery[39]=129;
centery[40]=122;
centery[41]=120;
centery[42]=112;
centery[43]=113;
centery[44]=112;
centery[45]=112;
centery[46]=113;
centery[47]=112;
centery[48]=111;
centery[49]=112;
centery[50]=113;
centery[51]=109;
centery[52]=108;
centery[53]=108;
centery[54]=107;
centery[55]=107;
centery[56]=106;
centery[57]=106;
centery[58]=109;
centery[59]=109;
centery[60]=110;
centery[61]=111;
centery[62]=111;
centery[63]=109;
centery[64]=104;
centery[65]=104;
centery[66]=106;
centery[67]=106;
centery[68]=104;
centery[69]=103;
centery[70]=104;
centery[71]=106;
centery[72]=107;
centery[73]=109;
centery[74]=107;
centery[75]=107;
centery[76]=107;
centery[77]=107;
centery[78]=108;
centery[79]=107;
centery[80]=106;
centery[81]=105;
centery[82]=104;
centery[83]=106;
centery[84]=108;
centery[85]=106;
centery[86]=106;
centery[87]=108;
centery[88]=111;
centery[89]=109;
centery[90]=110;
centery[91]=108;
centery[92]=112;
centery[93]=112;
centery[94]=107;
centery[95]=106;
centery[96]=105;
centery[97]=104;
centery[98]=106;
centery[99]=104;
centery[100]=104;
centery[101]=105;
centery[102]=109;
centery[103]=108;
centery[104]=107;
centery[105]=106;
centery[106]=107;
centery[107]=108;
centery[108]=108;
centery[109]=108;
centery[110]=108;
centery[111]=109;
centery[112]=109;
centery[113]=109;
centery[114]=110;
centery[115]=113;
centery[116]=116;
centery[117]=116;
centery[118]=118;
centery[119]=117;
centery[120]=120;
centery[121]=120;
centery[122]=123;
centery[123]=127;
centery[124]=129;
centery[125]=130;
centery[126]=130;
centery[127]=131;
centery[128]=132;
centery[129]=134;
centery[130]=135;
centery[131]=137;
centery[132]=138;
centery[133]=140;
centery[134]=141;
centery[135]=143;
centery[136]=145;
centery[137]=147;
centery[138]=149;
centery[139]=150;
centery[140]=154;
centery[141]=155;
centery[142]=156;
centery[143]=158;
centery[144]=159;
centery[145]=161;
centery[146]=162;
centery[147]=163;
centery[148]=164;
centery[149]=167;
centery[150]=167;
centery[151]=169;
centery[152]=170;
centery[153]=172;
centery[154]=173;
centery[155]=174;
centery[156]=176;
centery[157]=180;
centery[158]=182;
centery[159]=182;
centery[160]=184;
centery[161]=185;
centery[162]=187;
centery[163]=190;
centery[164]=193;
centery[165]=196;
centery[166]=197;
centery[167]=203;
centery[168]=205;
centery[169]=207;
centery[170]=210;
centery[171]=210;
centery[172]=212;
centery[173]=213;
centery[174]=217;
centery[175]=218;
centery[176]=221;
centery[177]=223;
centery[178]=224;
centery[179]=227;
centery[180]=230;
centery[181]=234;


	}

}