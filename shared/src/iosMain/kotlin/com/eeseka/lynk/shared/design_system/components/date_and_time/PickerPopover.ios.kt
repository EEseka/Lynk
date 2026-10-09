package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UILayoutFittingCompressedSize
import platform.UIKit.UILayoutPriorityFittingSizeLevel
import platform.UIKit.UILayoutPriorityRequired
import platform.UIKit.UIModalPresentationNone
import platform.UIKit.UIModalPresentationPopover
import platform.UIKit.UIModalPresentationStyle
import platform.UIKit.UIPopoverPresentationControllerDelegateProtocol
import platform.UIKit.UIPresentationController
import platform.UIKit.UITraitCollection
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleBottomMargin
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleLeftMargin
import platform.UIKit.UIViewAutoresizingFlexibleRightMargin
import platform.UIKit.UIViewAutoresizingFlexibleTopMargin
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject

private const val POPOVER_PADDING = 8.0
private const val SCREEN_MARGIN = 16.0

internal class PickerPopover(
    private val contentController: UIViewController,
    private val screenCover: UIView,
    @Suppress("unused")
    private val retainedObjects: List<Any>
) {
    fun close() {
        screenCover.removeFromSuperview()
        if (contentController.presentingViewController != null) {
            contentController.dismissViewControllerAnimated(true, completion = null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun presentPickerPopover(
    host: UIViewController,
    pickerView: UIView,
    tintColor: Color,
    onDismissedByUser: () -> Unit,
    retainedObjects: List<Any> = emptyList()
): PickerPopover {
    var presenter = host
    while (presenter.presentedViewController != null) {
        presenter = presenter.presentedViewController!!
    }

    val contentController = UIViewController()
    contentController.view.tintColor = tintColor.toUIColor()
    pickerView.translatesAutoresizingMaskIntoConstraints = false
    contentController.view.addSubview(pickerView)
    NSLayoutConstraint.activateConstraints(
        listOf(
            pickerView.topAnchor.constraintEqualToAnchor(contentController.view.topAnchor, constant = POPOVER_PADDING),
            pickerView.bottomAnchor.constraintEqualToAnchor(contentController.view.bottomAnchor, constant = -POPOVER_PADDING),
            pickerView.leadingAnchor.constraintEqualToAnchor(contentController.view.leadingAnchor, constant = POPOVER_PADDING),
            pickerView.trailingAnchor.constraintEqualToAnchor(contentController.view.trailingAnchor, constant = -POPOVER_PADDING)
        )
    )

    val screen = presenter.view.window ?: presenter.view
    val screenCover = ScreenCover(
        onWidthChanged = { screenWidth -> contentController.fitPicker(pickerView, screenWidth) }
    )
    screenCover.setFrame(screen.bounds)
    screenCover.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
    screen.addSubview(screenCover)
    contentController.fitPicker(pickerView, screen.bounds.useContents { size.width })

    // A zero-size view pinned to the middle of the screen, so the popover stays centred through a rotation
    val anchor = UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0))
    anchor.userInteractionEnabled = false
    anchor.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin or
            UIViewAutoresizingFlexibleRightMargin or
            UIViewAutoresizingFlexibleTopMargin or
            UIViewAutoresizingFlexibleBottomMargin
    anchor.setCenter(screenCover.bounds.useContents { CGPointMake(size.width / 2, size.height / 2) })
    screenCover.addSubview(anchor)

    val delegate = PickerPopoverDelegate(onDismissedByUser)
    contentController.modalPresentationStyle = UIModalPresentationPopover
    contentController.popoverPresentationController?.let { popover ->
        popover.sourceView = anchor
        popover.sourceRect = anchor.bounds
        popover.permittedArrowDirections = 0uL
        popover.delegate = delegate
    }
    presenter.presentViewController(contentController, animated = true, completion = null)

    return PickerPopover(
        contentController = contentController,
        screenCover = screenCover,
        retainedObjects = retainedObjects + delegate
    )
}

// The picker's own smallest width, kept inside the screen; Apple's calendar wants 391pt, wider than a 390pt iPhone
@OptIn(ExperimentalForeignApi::class)
private fun UIViewController.fitPicker(pickerView: UIView, screenWidth: Double) {
    val maxPickerWidth = screenWidth - (SCREEN_MARGIN + POPOVER_PADDING) * 2
    val pickerWidth = minOf(
        pickerView.systemLayoutSizeFittingSize(UILayoutFittingCompressedSize.readValue()).useContents { width },
        maxPickerWidth
    )
    val pickerHeight = pickerView.systemLayoutSizeFittingSize(
        CGSizeMake(pickerWidth, 0.0),
        withHorizontalFittingPriority = UILayoutPriorityRequired,
        verticalFittingPriority = UILayoutPriorityFittingSizeLevel
    ).useContents { height }
    setPreferredContentSize(CGSizeMake(pickerWidth + POPOVER_PADDING * 2, pickerHeight + POPOVER_PADDING * 2))
}

@OptIn(ExperimentalForeignApi::class)
private class ScreenCover(
    private val onWidthChanged: (screenWidth: Double) -> Unit
) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {

    private var lastWidth = 0.0

    override fun layoutSubviews() {
        super.layoutSubviews()
        val width = bounds.useContents { size.width }
        if (width != lastWidth) {
            lastWidth = width
            onWidthChanged(width)
        }
    }
}

private class PickerPopoverDelegate(
    private val onDismissedByUser: () -> Unit
) : NSObject(), UIPopoverPresentationControllerDelegateProtocol, UIAdaptivePresentationControllerDelegateProtocol {

    // iPhone turns popovers into sheets unless told not to
    override fun adaptivePresentationStyleForPresentationController(
        controller: UIPresentationController,
        traitCollection: UITraitCollection
    ): UIModalPresentationStyle = UIModalPresentationNone

    // Only called for a tap outside, never for a close we make ourselves
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        onDismissedByUser()
    }
}

private fun Color.toUIColor(): UIColor = UIColor.colorWithRed(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble()
)
