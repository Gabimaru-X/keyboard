package gnosis.keyboard.revamped

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * Revamped UI Dashboard
 *
 * Exact visual match to the redesigned mockups:
 * - Pure black OLED background
 * - Soft rounded dark cards (#15161C)
 * - Cyan accent rings on numbered badges
 * - Glyph-only icons
 * - Floating glassmorphic bottom dock
 * - No update / about section on primary surfaces
 */
class RevampedUiDashboard(private val activity: Activity) {

    private val density = activity.resources.displayMetrics.density
    private fun dp(v: Float) = (v * density).toInt()

    var isDarkMode: Boolean = true
        private set

    private var onThemeChangedListener: ((Boolean) -> Unit)? = null

    fun setOnThemeChangedListener(listener: (Boolean) -> Unit) {
        this.onThemeChangedListener = listener
    }

    fun toggleTheme(): Boolean {
        isDarkMode = !isDarkMode
        onThemeChangedListener?.invoke(isDarkMode)
        return isDarkMode
    }

    fun buildDashboard(
        onOpenEngineSettings: () -> Unit,
        onOpenNeuralAi: () -> Unit,
        onOpenThemePicker: () -> Unit,
        onOpenQuickKeys: () -> Unit,
        onOpenAbout: () -> Unit
    ): View {
        // Palette matching the generated mockups exactly
        val bgColor = if (isDarkMode) Color.parseColor("#000000") else Color.parseColor("#F4F4F6")
        val cardBg = if (isDarkMode) Color.parseColor("#15161C") else Color.parseColor("#FFFFFF")
        val cardBorder = if (isDarkMode) Color.parseColor("#242630") else Color.parseColor("#E5E7EB")
        val textPrimary = if (isDarkMode) Color.WHITE else Color.parseColor("#111827")
        val textSecondary = if (isDarkMode) Color.parseColor("#8E92A4") else Color.parseColor("#6B7280")
        val accentColor = if (isDarkMode) Color.parseColor("#22D3EE") else Color.parseColor("#2563EB") // cyan accent
        val badgeBg = if (isDarkMode) Color.parseColor("#1A1B22") else Color.parseColor("#E5E7EB")
        val dotInactive = if (isDarkMode) Color.parseColor("#3A3D4A") else Color.parseColor("#CBD5E1")
        val dockBg = if (isDarkMode) Color.parseColor("#CC181A22") else Color.parseColor("#E6FFFFFF")

        val rootFrame = FrameLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
        }

        val scroll = ScrollView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(16f), dp(40f), dp(16f), dp(110f))
        }

        // -------------------------------------------------------------
        // 1. Header
        // -------------------------------------------------------------
        val headerRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(22f) }
        }

        val titleTv = TextView(activity).apply {
            text = "Revamped"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setTextColor(textPrimary)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        headerRow.addView(titleTv)

        fun createCircularGlyph(drawableRes: Int, onClick: () -> Unit): View {
            return FrameLayout(activity).apply {
                val s = dp(40f)
                layoutParams = LinearLayout.LayoutParams(s, s).apply { marginStart = dp(10f) }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(cardBg)
                    setStroke(dp(1f), cardBorder)
                }
                val iv = ImageView(activity).apply {
                    setImageResource(drawableRes)
                    setColorFilter(textPrimary, PorterDuff.Mode.SRC_IN)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                addView(iv)
                setOnClickListener { onClick() }
            }
        }

        headerRow.addView(createCircularGlyph(R.drawable.ic_sliders) { onOpenEngineSettings() })
        headerRow.addView(createCircularGlyph(
            if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon
        ) { toggleTheme() })

        content.addView(headerRow)

        // -------------------------------------------------------------
        // 2. Top two equal cards
        // -------------------------------------------------------------
        val bentoRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(150f)
            ).apply { bottomMargin = dp(12f) }
        }

        // Card 1 – Delight5 Core
        val card1 = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                marginEnd = dp(6f)
            }
            background = GradientDrawable().apply {
                cornerRadius = dp(22f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(16f), dp(16f), dp(16f), dp(16f))

            // Numbered badge with cyan ring
            val badge = TextView(activity).apply {
                text = "1"
                setTextColor(textPrimary)
                textSize = 14f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                val s = dp(32f)
                layoutParams = LinearLayout.LayoutParams(s, s)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(badgeBg)
                    setStroke(dp(2f), accentColor)
                }
            }
            addView(badge)

            addView(View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
            })

            val title = TextView(activity).apply {
                text = "Delight5 Core"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(textPrimary)
            }
            addView(title)

            val sub = TextView(activity).apply {
                text = "Active · Sub-ms Trellis"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(textSecondary)
                setPadding(0, dp(2f), 0, 0)
            }
            addView(sub)

            setOnClickListener { onOpenEngineSettings() }
        }
        bentoRow.addView(card1)

        // Card 2 – Native Monolith
        val card2 = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                marginStart = dp(6f)
            }
            background = GradientDrawable().apply {
                cornerRadius = dp(22f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(16f), dp(16f), dp(16f), dp(16f))

            val metricRow = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.BOTTOM
            }
            val bigNum = TextView(activity).apply {
                text = "35.2"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
                setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                setTextColor(textPrimary)
            }
            metricRow.addView(bigNum)
            val unit = TextView(activity).apply {
                text = " MB"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(textSecondary)
                setPadding(dp(2f), 0, 0, dp(3f))
            }
            metricRow.addView(unit)
            addView(metricRow)

            addView(View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
            })

            val title = TextView(activity).apply {
                text = "Native Monolith"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(textPrimary)
            }
            addView(title)

            val sub = TextView(activity).apply {
                text = "C++ Runtime · On-device"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(textSecondary)
                setPadding(0, dp(2f), 0, 0)
            }
            addView(sub)

            setOnClickListener {
                Toast.makeText(activity, "libintegrated_shared_object.so ready", Toast.LENGTH_SHORT).show()
            }
        }
        bentoRow.addView(card2)
        content.addView(bentoRow)

        // -------------------------------------------------------------
        // 3. TNT Spatial Targeting card with 3 dot matrices
        // -------------------------------------------------------------
        val card3 = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12f) }
            background = GradientDrawable().apply {
                cornerRadius = dp(22f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(18f), dp(18f), dp(18f), dp(18f))

            // Header row with badge + title
            val headerSec = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(16f) }
            }

            val badge2 = TextView(activity).apply {
                text = "2"
                setTextColor(textPrimary)
                textSize = 14f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                val s = dp(32f)
                layoutParams = LinearLayout.LayoutParams(s, s).apply { marginEnd = dp(12f) }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(badgeBg)
                    setStroke(dp(2f), accentColor)
                }
            }
            headerSec.addView(badge2)

            val title2 = TextView(activity).apply {
                text = "TNT Spatial Targeting"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(textPrimary)
            }
            headerSec.addView(title2)
            addView(headerSec)

            // Three matrix columns
            val matrixRow = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            fun createMatrixCol(label: String, activeDots: Set<Int>): View {
                return LinearLayout(activity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

                    val grid = LinearLayout(activity).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    for (r in 0..3) {
                        val row = LinearLayout(activity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER
                        }
                        for (c in 0..3) {
                            val idx = r * 4 + c
                            val isActive = activeDots.contains(idx)
                            val dot = View(activity).apply {
                                val s = dp(5f)
                                layoutParams = LinearLayout.LayoutParams(s, s).apply {
                                    setMargins(dp(3f), dp(3f), dp(3f), dp(3f))
                                }
                                background = GradientDrawable().apply {
                                    shape = GradientDrawable.OVAL
                                    setColor(if (isActive) accentColor else dotInactive)
                                }
                            }
                            row.addView(dot)
                        }
                        grid.addView(row)
                    }
                    addView(grid)

                    val lbl = TextView(activity).apply {
                        text = label
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                        setTextColor(textSecondary)
                        gravity = Gravity.CENTER
                        setPadding(0, dp(10f), 0, 0)
                    }
                    addView(lbl)
                }
            }

            matrixRow.addView(createMatrixCol("Delight", setOf(5, 6, 9, 10)))
            matrixRow.addView(createMatrixCol("Spatial", setOf(0, 5, 10, 15)))
            matrixRow.addView(createMatrixCol("Dynamic", setOf(3, 6, 9, 12)))
            addView(matrixRow)

            setOnClickListener { onOpenEngineSettings() }
        }
        content.addView(card3)

        // -------------------------------------------------------------
        // 4. Keystrokes Predicted metric card
        // -------------------------------------------------------------
        val card4 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(88f)
            ).apply { bottomMargin = dp(16f) }
            background = GradientDrawable().apply {
                cornerRadius = dp(22f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(20f), dp(16f), dp(20f), dp(16f))

            val leftCol = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val leftTitle = TextView(activity).apply {
                text = "Keystrokes Predicted"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(textSecondary)
            }
            leftCol.addView(leftTitle)

            val metricRow = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.BOTTOM
                setPadding(0, dp(4f), 0, 0)
            }
            val bigVal = TextView(activity).apply {
                text = "3,200"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
                setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                setTextColor(textPrimary)
            }
            metricRow.addView(bigVal)
            val unitVal = TextView(activity).apply {
                text = " words"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(textSecondary)
                setPadding(dp(4f), 0, 0, dp(4f))
            }
            metricRow.addView(unitVal)
            leftCol.addView(metricRow)
            addView(leftCol)

            // Small square icon button on the right
            val iconBox = FrameLayout(activity).apply {
                val s = dp(40f)
                layoutParams = LinearLayout.LayoutParams(s, s)
                background = GradientDrawable().apply {
                    cornerRadius = dp(12f).toFloat()
                    setColor(if (isDarkMode) Color.parseColor("#1F2129") else Color.parseColor("#F1F5F9"))
                    setStroke(dp(1f), cardBorder)
                }
                val iv = ImageView(activity).apply {
                    setImageResource(R.drawable.ic_layout)
                    setColorFilter(textSecondary, PorterDuff.Mode.SRC_IN)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                addView(iv)
                setOnClickListener { onOpenQuickKeys() }
            }
            addView(iconBox)

            setOnClickListener { onOpenNeuralAi() }
        }
        content.addView(card4)

        scroll.addView(content)
        rootFrame.addView(scroll)

        // -------------------------------------------------------------
        // 5. Floating bottom dock (exact match to mockup)
        // -------------------------------------------------------------
        val bottomDock = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                dp(300f),
                dp(58f)
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(28f)
            }
            background = GradientDrawable().apply {
                cornerRadius = dp(29f).toFloat()
                setColor(dockBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(8f), 0, dp(8f), 0)
            elevation = dp(12f).toFloat()
        }

        fun createDockTab(drawableRes: Int, onClick: () -> Unit): View {
            return FrameLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                val iv = ImageView(activity).apply {
                    setImageResource(drawableRes)
                    setColorFilter(textPrimary, PorterDuff.Mode.SRC_IN)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(16f), dp(16f), dp(16f), dp(16f))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                addView(iv)
                setOnClickListener { onClick() }
            }
        }

        bottomDock.addView(createDockTab(R.drawable.ic_home) {
            scroll.smoothScrollTo(0, 0)
        })
        bottomDock.addView(createDockTab(R.drawable.ic_sliders) {
            onOpenEngineSettings()
        })
        bottomDock.addView(createDockTab(R.drawable.ic_sparkles) {
            onOpenNeuralAi()
        })
        bottomDock.addView(createDockTab(R.drawable.ic_palette) {
            onOpenThemePicker()
        })

        rootFrame.addView(bottomDock)
        return rootFrame
    }
}
