package com.jfleischer.slideshow.models;

public class FileItem {
	public enum Type {
		PARENT, DIR, DOC
	}

	final public Type type;
	final public String name;

	public FileItem(Type t, String n) {
		type = t;
		name = n;
	}
}
