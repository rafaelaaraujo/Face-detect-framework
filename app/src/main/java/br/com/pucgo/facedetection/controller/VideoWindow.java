package br.com.pucgo.facedetection.controller;

public class VideoWindow {
	private int screenWidth = 640;
	private int screenHeight = 480;
	// Constructor	
	
	// SET functions
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}
	
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}
	
	public void setScreen(int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}
	
	// GET functions
	public int  getScreenWidth() {
		return this.screenWidth;
	}
	
	public int getScreenHeight() {
		return this.screenHeight;
	}
}
