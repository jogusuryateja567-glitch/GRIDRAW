package com.gridraw.app

import android.app.Application
import com.gridraw.app.data.ProjectDatabase

class GridRawApplication : Application() {
    val database by lazy { ProjectDatabase.getDatabase(this) }
}
