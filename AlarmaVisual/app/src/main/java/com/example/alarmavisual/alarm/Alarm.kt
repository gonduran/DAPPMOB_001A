package com.example.alarmavisual.alarm

import java.util.Calendar

data class Alarm(
    val id: String = "",  // Valor predeterminado
    val time: String = "",  // Valor predeterminado
    val days: List<String> = emptyList(),  // Valor predeterminado
    val active: Boolean = false,  // Valor predeterminado
    val label: String = "",  // Valor predeterminado
    val repeat: Boolean = false  // Valor predeterminado
) {
    fun getNextTriggerTime(): Long {
        val (hour, minute) = time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance()

        // Definir un mapa con los días de la semana
        val daysOfWeekMap = mapOf(
            "Lun" to Calendar.MONDAY,
            "Mar" to Calendar.TUESDAY,
            "Mié" to Calendar.WEDNESDAY,
            "Jue" to Calendar.THURSDAY,
            "Vie" to Calendar.FRIDAY,
            "Sáb" to Calendar.SATURDAY,
            "Dom" to Calendar.SUNDAY
        )

        // Buscar el próximo día en el que la alarma se ejecutará
        for (i in 0..7) {
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayOfWeekString = daysOfWeekMap.entries.find { it.value == currentDayOfWeek }?.key

            // Comparar el día actual con la lista de días seleccionados
            if (dayOfWeekString != null && days.contains(dayOfWeekString)) {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    // Si el tiempo ya ha pasado hoy, agrega un día
                    if (timeInMillis < System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                return calendar.timeInMillis
            }

            // Avanzar al siguiente día
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return Long.MAX_VALUE // Retornar un valor grande si no hay día seleccionado
    }
}
