package com.jackby03.sparky.core.constants

object AppConstants {

    const val DATABASE_NAME = "spark_database"

    object Canvas {
        const val GRID_SIZE_DP      = 20f
        const val MIN_ZOOM          = 0.25f
        const val MAX_ZOOM          = 3.0f
        const val DEFAULT_ZOOM      = 1.0f
        const val SNAP_THRESHOLD_DP = 10f
        const val COMPONENT_SIZE_DP = 48f
    }

    object Simulation {
        const val MAX_ITERATIONS         = 100
        const val CONVERGENCE_THRESHOLD  = 1e-9
        const val DEFAULT_TIME_STEP_S    = 1e-6
    }

    object Export {
        const val DEFAULT_IMAGE_QUALITY = 95
        const val DEFAULT_DPI           = 300
        const val PDF_MARGIN_MM         = 10f
    }

    object Defaults {
        const val RESISTANCE_OHM     = 1_000.0   // 1 kΩ
        const val CAPACITANCE_FARAD  = 1e-6       // 1 µF
        const val INDUCTANCE_HENRY   = 1e-3       // 1 mH
        const val VOLTAGE_VOLTS      = 5.0        // 5 V
        const val CURRENT_AMPERES    = 1e-3       // 1 mA
        const val FORWARD_VOLTAGE_V  = 0.7        // Diode Vf
    }
}
