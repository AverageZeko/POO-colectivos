module POO.colectivos {
	// Le dice a tu proyecto que necesita usar los módulos de JavaFX
	requires javafx.controls;
	
	// Permite que JavaFX acceda a tu paquete para poder lanzar la ventana
	opens colectivo.interfaz to javafx.graphics;
}