package com.jackby03.sparky.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jackby03.sparky.data.local.database.SparkDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented migration tests for [SparkDatabase].
 *
 * [MigrationTestHelper] reads the Room schema files from the test assets directory.
 * Those JSON files are generated automatically by KSP on each build (room.schemaLocation
 * is set in build.gradle.kts) and exposed as androidTest assets via sourceSets.
 *
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class SparkDatabaseMigrationTest {

    companion object {
        private const val TEST_DB = "sparky-migration-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SparkDatabase::class.java
    )

    /**
     * Verifies that MIGRATION_1_2 successfully adds the updatedAt index without
     * destroying any existing circuit or project rows.
     */
    @Test
    fun migrate1To2_addsIndexOnCircuitsUpdatedAt() {
        // Create a v1 database via the exported schema (no index yet)
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert a circuit row so we can verify data survives the migration
            execSQL(
                "INSERT INTO circuits (id, projectId, name, componentsJson, wiresJson, createdAt, updatedAt) " +
                "VALUES ('c1', 'p1', 'Test Circuit', '[]', '[]', 1000, 2000)"
            )
            close()
        }

        // Run MIGRATION_1_2 and validate the resulting schema against the v2 export
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, SparkDatabase.MIGRATION_1_2)

        // Confirm the index now exists in sqlite_master
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='index_circuits_updatedAt'"
        )
        assert(cursor.count == 1) {
            "Expected index 'index_circuits_updatedAt' to exist after MIGRATION_1_2"
        }
        cursor.close()

        // Confirm the pre-existing row survived (migration should not drop data)
        val rowCursor = db.query("SELECT id FROM circuits WHERE id='c1'")
        assert(rowCursor.count == 1) {
            "Circuit row must survive MIGRATION_1_2 without data loss"
        }
        rowCursor.close()
    }
}
