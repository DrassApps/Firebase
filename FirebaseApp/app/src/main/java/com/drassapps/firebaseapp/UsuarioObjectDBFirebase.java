package com.drassapps.firebaseapp;

/**
 *  Representa un objeto de la BD de Firebase
 */

public class UsuarioObjectDBFirebase  {

    public String Token;
    public String Nombre;
    public String Contraseña;

    public UsuarioObjectDBFirebase(){
        // Es necesario indicar el constructor por defecto para llamar a
        // DataSnapshot.getValue(User.class)
    }

    public UsuarioObjectDBFirebase(String Nombre, String Token, String Contraseña) {
        this.Nombre = Nombre;
        this.Token = Token;
        this.Contraseña= Contraseña;
    }

}
