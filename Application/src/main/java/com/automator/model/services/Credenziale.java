package com.automator.model.services;

public class Credenziale {
	  private String email;
	    private String password;

	    public Credenziale(String email, String password) {
	        this.email = email;
	        this.password = password;
	    }
	    public String getEmail() { return email; }
	    public String getPassword() { return password; }
}
