package com.example.bachelor

import java.awt.FlowLayout
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel


class DisplayImage(img: BufferedImage) {

    init {
        val icon = ImageIcon(img)
        val frame = JFrame()
        frame.layout = FlowLayout()
        frame.setSize(200, 300)
        val lbl = JLabel()
        lbl.icon = icon
        frame.add(lbl)
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }
}