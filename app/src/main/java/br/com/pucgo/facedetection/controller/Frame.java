package br.com.pucgo.facedetection.controller;

public class Frame {
	private String imgName;
	private int duration;
	
	public Frame(String imgName, int duration) {
		this.imgName = imgName;
		this.duration = duration;
	}
	
	// SET ===============================================
	public void setImageName(String imgName) {
		this.imgName = imgName;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	// GET ===============================================
	public String getImageName() {
		return this.imgName;
	}
	
	public int getDuration() {
		return this.duration;
	}

}
