package br.com.pucgo.facedetection.controller;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

public class CreateMP4Video {
	private Context context;
	private String output;
	private String quality;
	private SequenceImagesEncoder encoder = null;
	private int windowWidth;
	private int windowHeight;
	private int currentFrame;
	
	private FramePackage framePackage;
	
	// Constructor
	public CreateMP4Video(Context context, FramePackage framePackage, String output, String quality) {
		this.context = context;
		this.output = output;
		this.quality = quality;
		this.framePackage = framePackage;
		this.currentFrame = 0;
	}
	
	// SET functions ===================================================================
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public void setQuality(String quality) {
		this.quality = quality.toUpperCase();
	}
	
	public void setVideoQuality(String quality) {
		switch (quality)
		{
			case "MOBILE":
			{
				windowWidth = 320;
				windowHeight = 240;
			}
			break;
			case "TV":
			{
				windowWidth = 640;
				windowHeight = 480;
			}
			break;
			case "DVD":
			{
				windowWidth = 720;
				windowHeight = 576;
			}
			break;
			case "HD":
			{
				windowWidth = 1280;
				windowHeight = 720;
			}
			break;
			case "FULLHD":
			{
				windowWidth = 1920;
				windowHeight = 1080;
			}
			break;
			default:
			{
				windowWidth = 640;
				windowHeight = 480;
			}
		}
	}
	
	// GET functions ===================================================================
	public Context getContext() {
		return this.context;
	}
	
	public String getOutput() {
		return this.output;
	}
	
	public String getQuality() {
		return this.quality;
	}
	
	public int getCurrentFrame() {
		return this.currentFrame;
	}

	// Main function ===================================================================
	public void verifyQuality() {
		setVideoQuality(quality);
	}
	
	public void PrepareForEncoder() throws IOException {
		encoder = new SequenceImagesEncoder(new File(output),windowWidth,windowHeight);
	}
	
	public void PrepareForScreen() {
		VideoWindow videoWindow = new VideoWindow();
		videoWindow.setScreen(windowWidth, windowHeight);
	}
	
	public void addFrameToVideo(int frameNumber) throws IOException {
		Bitmap originalImage = null;
    	Bitmap resizedImage = null;

    	if (frameNumber >= 0 && frameNumber < framePackage.getCount()) {
//    		originalImage = BitmapHandler.getBitmapFromSDCard(framePackage.getFrameAt(frameNumber + 1).getImageName());
//    		resizedImage = BitmapHandler.resizeImage(originalImage, windowWidth, windowHeight);
    		if (frameNumber == 0) {
    			encoder.encodeImage(framePackage.getFrameAt(frameNumber + 1),0);
    		} else {
    			int duration = 1;
    			encoder.encodeImage(framePackage.getFrameAt(frameNumber + 1),duration);
    		}
    		currentFrame = frameNumber+1;
    	}
	}
	
	public void Completing() throws IOException {
		// create a black bitmap for background the last picture
//		Bitmap blackImage = Bitmap.createBitmap(windowWidth, windowHeight, Config.ARGB_8888);
//		Canvas c = new Canvas(blackImage);
//		c.drawRGB(0, 0, 0);
//		c.drawBitmap(blackImage, new Matrix(), null);
//
//		encoder.encodeImage(blackImage,1000);
    	
    	encoder.finish();
	}
	
	public void CreateVideo() throws IOException {
		verifyQuality();

		PrepareForEncoder();

		PrepareForScreen();

		for (int i = 0; i < framePackage.getCount(); ++i) {
			addFrameToVideo(i);
		}


    	Completing();
    	
	}
}
