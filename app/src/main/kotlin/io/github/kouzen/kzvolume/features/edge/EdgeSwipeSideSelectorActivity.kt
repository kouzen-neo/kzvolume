package io.github.kouzen.kzvolume.features.edge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.kouzen.kzvolume.Constants
import io.github.kouzen.kzvolume.core.preferences.AppPreferences
import io.github.kouzen.kzvolume.databinding.ActivityEdgeSwipeSideSelectorBinding

class EdgeSwipeSideSelectorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEdgeSwipeSideSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdgeSwipeSideSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePad = (16 * resources.displayMetrics.density).toInt()
            v.setPadding(
                systemBars.left + basePad,
                systemBars.top + basePad,
                systemBars.right + basePad,
                systemBars.bottom + basePad
            )
            insets
        }

        val selectedSide = AppPreferences.getEdgeSelectedSide(this)
        if (selectedSide == Constants.SIDE_LEFT) {
            binding.leftSideRadio.isChecked = true
        } else {
            binding.rightSideRadio.isChecked = true
        }

        binding.saveEdgeButton.setOnClickListener {
            val side = if (binding.leftSideRadio.isChecked) {
                Constants.SIDE_LEFT
            } else {
                Constants.SIDE_RIGHT
            }

            AppPreferences.setEdgeSelectedSide(this, side)
            AppPreferences.setEdgeConfigured(this, true)
            EdgeOverlayRuntime.sync(this)
            finish()
        }
    }
}
