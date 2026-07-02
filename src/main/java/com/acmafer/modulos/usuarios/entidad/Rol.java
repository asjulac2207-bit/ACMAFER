package com.acmafer.modulos.usuarios.entidad;

/**
 * Roles del sistema ACMAFER.
 * Respaldo: SRS §2.3, ObtenerIdRolPorNombre C#, tabla rol BD.
 * VENDEDOR: implementación nueva requerida por SRS §2.2 y RF-04/24/34.
 */
public enum Rol {
    ADMINISTRADOR,  // idRol=1 en C# original
    SUPERVISOR,     // idRol=4 en C# original
    TRABAJADOR,     // idRol=2 "EMPLEADO" en C# — renombrado para alinear con SRS
    VENDEDOR,       // No implementado en C#. Requerido por SRS §2.2, RF-04, RF-24, RF-34
    CLIENTE         // idRol=3 en C# original. No en SRS explícitamente, sí en BD y código
}
