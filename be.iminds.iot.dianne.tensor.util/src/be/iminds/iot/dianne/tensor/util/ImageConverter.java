/*******************************************************************************
 * DIANNE  - Framework for distributed artificial neural networks
 * Copyright (C) 2015  iMinds - IBCN - UGent
 *
 * This file is part of DIANNE.
 *
 * DIANNE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Tim Verbelen, Steven Bohez
 *******************************************************************************/
package be.iminds.iot.dianne.tensor.util;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import javax.imageio.plugins.jpeg.JPEGQTable;
import javax.imageio.stream.ImageInputStream;

import com.idrsolutions.image.jpeg.JpegDecoder;
import com.idrsolutions.image.jpeg.JpegEncoder;

import be.iminds.iot.dianne.tensor.Tensor;

/**
 * Converts Tensors from and to BufferedImage / JPEG data
 * @author tverbele
 *
 */
public class ImageConverter {

	// default huffmann code tables for UVC video devices' MJPEG format
	private final short[] dc_lum_len = {0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0};
	private final short[] dc_lum_val = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	private final short[] dc_chr_len = {0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
	private final short[] dc_chr_val = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	
	private final short[] ac_lum_len = {0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d};
	private final short[] ac_lum_val = {
			   0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21,
			   0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71,
			   0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1,
			   0xc1, 0x15, 0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72,
			   0x82, 0x09, 0x0a, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x25,
			   0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36, 0x37,
			   0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48,
			   0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
			   0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a,
			   0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x83,
			   0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93,
			   0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3,
			   0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3,
			   0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3,
			   0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3,
			   0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2,
			   0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1,
			   0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa};
	private final short[] ac_chr_len = {0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77};
	private final short[] ac_chr_val = {
			   0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31,
			   0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22,
			   0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1,
			   0x09, 0x23, 0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1,
			   0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25, 0xf1, 0x17, 0x18,
			   0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36,
			   0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47,
			   0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
			   0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
			   0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a,
			   0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a,
			   0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a,
			   0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa,
			   0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba,
			   0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca,
			   0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda,
			   0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea,
			   0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa};
	
	
	private ImageReader ioDecoder;
	private JPEGImageReadParam param;
	
	public ImageConverter(){
		this.ioDecoder = ImageIO.getImageReadersByFormatName("JPEG").next();
		this.param = new JPEGImageReadParam();
		this.param.setDecodeTables(new JPEGQTable[]{}, createDCHuffmanTables(), createACHuffmanTables());
	}
	
	private JPEGHuffmanTable[] createACHuffmanTables() {
		JPEGHuffmanTable acChm = new JPEGHuffmanTable(ac_chr_len, ac_chr_val);
		JPEGHuffmanTable acLum = new JPEGHuffmanTable(ac_lum_len, ac_lum_val);
		JPEGHuffmanTable[] result = { acLum, acChm };
		return result;
	}

	private JPEGHuffmanTable[] createDCHuffmanTables() {
		JPEGHuffmanTable dcChm = new JPEGHuffmanTable(dc_chr_len, dc_chr_val);
		JPEGHuffmanTable dcLum = new JPEGHuffmanTable(dc_lum_len, dc_lum_val);
		JPEGHuffmanTable[] result = { dcLum, dcChm };
		return result;
	}

	public Tensor readFromImage(BufferedImage img){
		int width = img.getWidth();
		int height = img.getHeight();
		
		float[] imageData = new float[width*height*3];
		
		float[] rgb = new float[3];
		int r = 0;
		int g = width*height;
		int b = 2*width*height;
		for(int j=0;j<height;j++){
			for(int i=0;i<width;i++){
				rgb = img.getRaster().getPixel(i, j, rgb);
				imageData[r++] = rgb[0]/255f;
				imageData[g++] = rgb[1]/255f;
				imageData[b++] = rgb[2]/255f;
			}
		}
		
		return new Tensor(imageData, 3, height, width);
	}
	
	public Tensor readFromFile(String fileName) throws Exception{
		// jDeli should be faster
		//BufferedImage img = ImageIO.read(new File(fileName));

		byte[] data = Files.readAllBytes(Paths.get(fileName));
		JpegDecoder decoder = new JpegDecoder();
		BufferedImage img = decoder.read(data);
		return readFromImage(img);
	}
	
	
	public Tensor readFromBytes(byte[] data) throws Exception {
        ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
        ioDecoder.setInput(stream, true, true);
		BufferedImage img = ioDecoder.read(0, param);

		// jDeli decoder not always working with mjpeg data
		// especially not when no Huffmann table is given, which could be fixed for imageio decoder
		//BufferedImage img = decoder.read(data);
		return readFromImage(img);
	}

	public BufferedImage writeToImage(Tensor t) throws Exception {
		int width, height, channels;
		
		if(t.dim()==3){
			channels = t.dims()[0];
			height = t.dims()[1];
			width = t.dims()[2];
		} else if(t.dim()==2){
			channels = 1;
			height = t.dims()[0];
			width = t.dims()[1];
		} else {
			throw new Exception("Wrong dimensions of tensor");
		}
		
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		
		float[] data = t.get();
		int c1 = 0;
		int c2 = width*height;
		int c3 = 2*width*height;
		
		int r=0,g=0,b=0,a=0,col;
		
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				if(channels==1){
					int val = (int)(data[c1++]*255f);
					r = val;
					g = val;
					b = val;
					a = 255;
				} else if(channels==3){
					r = (int)(data[c1++]*255f);
					g = (int)(data[c2++]*255f);
					b = (int)(data[c3++]*255f);
					a = 255;
				}

				col = a << 24 | r << 16 | g << 8 | b;
				img.setRGB(i, j, col);
			}
		}
		return img;
	}
	
	public void writeToFile(String fileName, Tensor t) throws Exception{
		BufferedImage img = writeToImage(t);
		
		JpegEncoder encoder = new JpegEncoder();
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
		encoder.write(img, output);
		output.flush();
		output.close();
		
		// jDeli should be faster
		//String formatName = fileName.substring(fileName.length()-3);
		//ImageIO.write(img, formatName, new File(fileName));
	}
}
