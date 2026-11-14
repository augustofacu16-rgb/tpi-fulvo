package com.fulvo.operaciones.port;

public interface GeocodificacionPort {

    UbicacionResuelta resolver(String direccion, Double latitudManual, Double longitudManual);
}
