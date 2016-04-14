package Crackcode;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;




public class Main {

	//private static Map<BufferedImage, String> trainMap = null;
	
	
	 public final static String srcPath = new File("").getAbsolutePath()+"\\srcimg\\";
	 public final static String trainPath = new File("").getAbsolutePath()+"\\trainimg\\";
		
	/**
	 * 下载验证码
	 * @param url
	 * @param imgName  验证码的文件名
	 * @return
	 **/
	public static String downloadImage(String url, String imgName) throws MalformedURLException {
		URL murl;
		try {
			murl = new URL(url);
			URLConnection connection=murl.openConnection();
			InputStream is=connection.getInputStream();		
			//InputStreamReader inputStreamReader=new InputStreamReader(is);  //没有使用
			OutputStream outputStream=new FileOutputStream(new File(srcPath+imgName));
			int length = -1;
			byte[] bytes = new byte[1024];
			while((length = is.read(bytes)) != -1){
				outputStream.write(bytes, 0, length);		//保存图片到本地
			}
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(srcPath+imgName);
		return srcPath+imgName;
	}

	/**
	 * 判断是否为蓝色
	 * 如果是，就返回 1 否则返回 0
	 **/
	public static int isBlue(int colorInt) {  	
        Color color = new Color(colorInt);  
        int rgb = color.getRed() + color.getGreen() + color.getBlue();
        if (rgb == 153) {  		// 等于 153 就是蓝色？
            return 1;  
        }  
        return 0;  
    }  
	
	
	/**
	 * 判断是否为Black
	 * 如果是，就返回 1 否则返回 0
	 **/
	public static int isBlack(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
			return 1;
		}
		
		return 0;
	}

	
	/**
	 * 去除背景
	 * 如果是，就返回 1 否则返回 0
	 **/
	public static BufferedImage removeBackgroud(String picFile)
			throws Exception {
		BufferedImage img = ImageIO.read(new File(picFile));  
        img = img.getSubimage(5, 1, img.getWidth()-5, img.getHeight()-2); 	
        img = img.getSubimage(0, 0, 50, img.getHeight());
		

        
        
        System.out.println("正在生成处理后的图片");
        ImageIO.write(img, "png", new File(srcPath+"预处理.png"));
		
        int width = img.getWidth();  
        int height = img.getHeight();  
        for(int x=0; x<width; x++){
        	for(int y=0; y<height; y++){
        		if(isBlue(img.getRGB(x, y)) == 1){
        			img.setRGB(x, y, Color.BLACK.getRGB());		// 如果是蓝色 就全部设置为黑色
        		}else{
        			img.setRGB(x, y, Color.WHITE.getRGB());		// 否则 设置为白色
        		}
        	}
        }
        System.out.println("正在生成处理后的图片");
        ImageIO.write(img, "png", new File(srcPath+"预处理2.png"));
        return img;  
	}
	
	public static List<BufferedImage> splitImage(BufferedImage img)		// 拆分验证码
			throws Exception {
		List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int width = img.getWidth()/4;
		int height = img.getHeight();			
		subImgs.add(img.getSubimage(0, 0, width, height));				// 直接平均拆分成4块？
		subImgs.add(img.getSubimage(width, 0, width, height));
		subImgs.add(img.getSubimage(width*2, 0, width, height));
		subImgs.add(img.getSubimage(width*3, 0, width, height));
		
		int i = 0;
		for (BufferedImage bi : subImgs) {
			
	        System.out.println("正在生成处理后的图片");
	        ImageIO.write(bi, "png", new File(srcPath+"分割"+i+".png"));
			i++;
		}
		
		
		return subImgs;
	}

	public static Map<BufferedImage, String> loadTrainData() throws Exception {
		//if (trainMap == null) {
			Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
			File dir = new File(trainPath);			
			File[] files = dir.listFiles();			// 从trainPath中加载文件
			for (File file : files) {
				map.put(ImageIO.read(file), file.getName().charAt(0) + "");	// 加载样本数据      charAt(0) 只取文件名的第一个值
			}
		//	trainMap = map;	
		//}
		return map;
	}

	
	/**
	 * 图片匹配核心代码
	 * 
	 **/
	public static String getSingleCharOcr(BufferedImage img,
			Map<BufferedImage, String> map) {
		String result = "#";
		int width = img.getWidth();			// 得到宽度
		int height = img.getHeight();		// 得到高度
		int min = width * height;			// 像素个数
		for (BufferedImage bi : map.keySet()) {
			int count = 0;
			
			if (Math.abs(bi.getWidth()-width) > 2)		// Math.abs取绝对值 
				continue;
			int widthmin = width < bi.getWidth() ? width : bi.getWidth();		// 取两者最小宽度、高度
			int heightmin = height < bi.getHeight() ? height : bi.getHeight();
			
			Label1: 
			
			for (int x = 0; x < widthmin; ++x) {
				for (int y = 0; y < heightmin; ++y) {
					if (isBlack(img.getRGB(x, y)) != isBlack(bi.getRGB(x, y))) {		// 与样本进行对比，积累误差
						count++;
						if (count >= min)			// 如果有匹配度更高的，就直接break
							break Label1;
					}
				}
			}
			
			if (count < min) {						// 如果当前的样本匹配度更高
				min = count;						// 记录当前的样本的误差值count
				result = map.get(bi);				// 记录样本的名字首字母
			}
		}
		
		System.out.println("识别结果："+result);		//打印识别结果
		System.out.println("误差值："+min);		//打印误差值count    最大误差值：width * height

		
		return result;
	}

	public static String getAllOcr(String file) throws Exception {
		BufferedImage img = removeBackgroud(file);  		// 移除背景杂乱的颜色干扰
		
		List<BufferedImage> listImg = splitImage(img);		// 拆分验证码 保存到 listImg
		Map<BufferedImage, String> map = loadTrainData();	// 加载训练数据
		String result = "";
		for (BufferedImage bi : listImg) {		
		
			result += getSingleCharOcr(bi, map);			// Ocr识别 将图片转换成文字
			
		}
		return result;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(trainPath);
		String pic=downloadImage("http://jwgl.gdut.edu.cn/CheckCode.aspx", "处理前.png"); 	// 从网络下载验证码
		
		//String pic = srcPath+"处理前.png";			// 加载本地图片
		String text = getAllOcr(pic);				// 识别验证码
		System.out.println("验证码："+text);			// 输出验证码

	}
	
}
