package com.eeseka.lynk.shared.presentation.preview

import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.PaymentState
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import com.eeseka.lynk.shared.domain.spot.model.BusinessStatus
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotAiSummary
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.spot.model.SpotPriceRange
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutPaymentUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutPreviewUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutSummaryUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.notification.model.NotificationUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotDayHoursUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotHighlight
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// Sample data shared by every @Preview in the app. Previews only — never read from production code.

const val PREVIEW_HOST_ID = "host-1"
const val PREVIEW_GUEST_ID = "guest-1"

val PREVIEW_SCHEDULED_AT: Instant = Instant.fromEpochSeconds(1_800_000_000L)
val PREVIEW_CREATED_AT: Instant = Instant.fromEpochSeconds(1_790_000_000L)

fun previewSpot(
    id: String = "s1",
    name: String = "Nok by Alara",
    category: SpotCategory = SpotCategory.RESTAURANT,
    priceLevel: PriceLevel? = PriceLevel.MODERATE,
    rating: Double? = 4.6,
    reviewCount: Int? = 214,
    isOpenNow: Boolean? = true,
    shortAddress: String? = "Victoria Island, Lagos",
    isSaved: Boolean = false
) = SpotUi(
    id = id,
    name = name,
    typeLabel = null,
    description = null,
    aiSummary = null,
    photoUrls = persistentListOf(),
    category = category,
    priceLevel = priceLevel,
    priceRange = null,
    rating = rating,
    reviewCount = reviewCount,
    businessStatus = null,
    isOpenNow = isOpenNow,
    nextOpenTime = null,
    nextCloseTime = null,
    utcOffsetMinutes = null,
    weekHours = persistentListOf(),
    highlights = persistentListOf(),
    shortAddress = shortAddress,
    latitude = 6.4281,
    longitude = 3.4219,
    phoneNumber = null,
    websiteUrl = null,
    googleMapsUrl = null,
    directionsUrl = null,
    isSaved = isSaved
)

val previewSpots: ImmutableList<SpotUi> = persistentListOf(
    previewSpot(),
    previewSpot(
        id = "s2",
        name = "Jazzhole",
        category = SpotCategory.CAFE,
        priceLevel = PriceLevel.CHEAP,
        rating = 4.4,
        reviewCount = 88,
        isOpenNow = false,
        shortAddress = "Ikoyi, Lagos",
        isSaved = true
    ),
    previewSpot(
        id = "s3",
        name = "The Rooftop Lounge",
        category = SpotCategory.LOUNGE,
        priceLevel = null,
        rating = 4.5,
        reviewCount = 120,
        shortAddress = "12 Admiralty Way, Lekki"
    )
)

val previewWeekHours: ImmutableList<SpotDayHoursUi> = persistentListOf(
    SpotDayHoursUi(day = "Monday", hours = "12:00 – 11:00 PM", isToday = false),
    SpotDayHoursUi(day = "Tuesday", hours = "12:00 – 11:00 PM", isToday = true),
    SpotDayHoursUi(day = "Wednesday", hours = "12:00 – 11:00 PM", isToday = false),
    SpotDayHoursUi(day = "Thursday", hours = "12:00 PM – 1:00 AM", isToday = false),
    SpotDayHoursUi(day = "Friday", hours = "12:00 PM – 2:00 AM", isToday = false),
    SpotDayHoursUi(day = "Saturday", hours = "12:00 PM – 2:00 AM", isToday = false),
    SpotDayHoursUi(day = "Sunday", hours = "Closed", isToday = false)
)

val previewFullSpot: SpotUi = previewSpot(isSaved = true).copy(
    typeLabel = "Nigerian restaurant",
    aiSummary = SpotAiSummary(
        text = "Upscale spot serving modern takes on Nigerian classics, with a buzzing bar and a garden for groups.",
        disclosure = "Summarized with Gemini"
    ),
    photoUrls = persistentListOf("places/preview/photos/1", "places/preview/photos/2"),
    priceRange = SpotPriceRange(currencyCode = "NGN", startAmount = 20_000, endAmount = 40_000),
    businessStatus = BusinessStatus.OPERATIONAL,
    nextCloseTime = PREVIEW_SCHEDULED_AT,
    weekHours = previewWeekHours,
    highlights = SpotHighlight.entries.filterNot { it == SpotHighlight.PAID_PARKING || it == SpotHighlight.CASH_ONLY }.toImmutableList(),
    phoneNumber = "+234 810 361 3662",
    websiteUrl = "https://www.nokbyalara.com/",
    googleMapsUrl = "https://maps.google.com/?cid=1",
    directionsUrl = "https://www.google.com/maps/dir//6.4281,3.4219"
)

// Index 0 is the host and index 1 is the guest, so previews can tell the two roles apart.
fun previewUser(index: Int) = HangoutUserUi(
    userId = when (index) {
        0 -> PREVIEW_HOST_ID
        1 -> PREVIEW_GUEST_ID
        else -> "user-$index"
    },
    username = if (index == 0) "the.host" else "guest$index",
    displayName = if (index == 0) "Ada Obi" else "Guest $index",
    initials = if (index == 0) "AO" else "G$index",
    profilePictureUrl = null
)

fun previewUsers(count: Int): ImmutableList<HangoutUserUi> =
    List(count) { previewUser(it) }.toImmutableList()

// The host is created already paid, exactly as the server does it.
fun previewParticipants(
    count: Int,
    paidGuestCount: Int = 0
): ImmutableList<HangoutParticipantUi> = List(count) { index ->
    HangoutParticipantUi(
        user = previewUser(index),
        rsvpStatus = RsvpStatus.ATTENDING,
        hasPaid = index == 0 || index <= paidGuestCount
    )
}.toImmutableList()

fun previewHangout(
    status: HangoutStatus = HangoutStatus.SCHEDULED,
    withSpot: Boolean = true,
    paymentState: PaymentState? = null,
    paidGuestCount: Int = 0
) = HangoutUi(
    id = "h1",
    hostId = PREVIEW_HOST_ID,
    name = "Rooftop Party in Lekki",
    description = "Bring your best vibes. We'll sort drinks and music, you just show up.",
    vibe = HangoutVibe.PARTY,
    status = status,
    scheduledAt = PREVIEW_SCHEDULED_AT,
    maxAttendees = 10,
    participantCount = 6,
    chosenSpot = if (withSpot) previewSpot() else null,
    participants = previewParticipants(count = 6, paidGuestCount = paidGuestCount),
    payment = paymentState?.let { previewPayment(it) },
    createdAt = PREVIEW_CREATED_AT
)

fun previewPayment(state: PaymentState = PaymentState.COLLECTING) = HangoutPaymentUi(
    totalCostKobo = 2_400_000L,
    costPerPersonKobo = 400_000L,
    splitHeadcount = 6,
    deadline = Instant.fromEpochSeconds(1_799_000_000L),
    state = state
)

// The invite sheet's slimmer hangout: attendees only, no payment or RSVP detail.
fun previewHangoutInvite(status: HangoutStatus = HangoutStatus.SCHEDULED) = HangoutPreviewUi(
    id = "h1",
    hostId = PREVIEW_HOST_ID,
    name = "Sunday Jollof Run",
    description = "Meeting up for the good stuff before the match starts.",
    vibe = HangoutVibe.FOOD,
    status = status,
    scheduledAt = PREVIEW_SCHEDULED_AT,
    maxAttendees = 8,
    participantCount = 5,
    chosenSpot = previewSpot(),
    attendees = previewUsers(4),
    createdAt = PREVIEW_CREATED_AT
)

fun previewHangoutSummary(
    id: String = "h1",
    name: String = "Rooftop Party in Lekki",
    vibe: HangoutVibe = HangoutVibe.PARTY,
    status: HangoutStatus = HangoutStatus.SCHEDULED,
    maxAttendees: Int? = 10,
    participantCount: Int = 6
) = HangoutSummaryUi(
    id = id,
    hostId = PREVIEW_HOST_ID,
    name = name,
    vibe = vibe,
    status = status,
    scheduledAt = PREVIEW_SCHEDULED_AT,
    maxAttendees = maxAttendees,
    participantCount = participantCount,
    createdAt = PREVIEW_CREATED_AT
)

val previewHangoutSummaries: ImmutableList<HangoutSummaryUi> = persistentListOf(
    previewHangoutSummary(
        id = "h1",
        name = "Late Night Drinks",
        vibe = HangoutVibe.DRINKS,
        status = HangoutStatus.VOTING,
        participantCount = 4
    ),
    previewHangoutSummary(id = "h2"),
    previewHangoutSummary(
        id = "h3",
        name = "Beach Day at Tarkwa Bay",
        vibe = HangoutVibe.ACTIVE,
        status = HangoutStatus.COMPLETED,
        maxAttendees = 8
    ),
    previewHangoutSummary(
        id = "h4",
        name = "Karaoke Night",
        vibe = HangoutVibe.PARTY,
        status = HangoutStatus.CANCELLED,
        maxAttendees = null,
        participantCount = 3
    )
)

// Relative to now, so the time labels read "3h ago" and "2d ago" instead of a fixed date.
val previewNotifications: ImmutableList<NotificationUi> = persistentListOf(
    NotificationUi(
        id = "n1",
        type = NotificationType.PARTICIPANT_INVITED,
        hangoutId = "h1",
        hangoutName = "Sunday Jollof Run",
        actorDisplayName = "Tolu",
        amountKobo = null,
        isRead = false,
        createdAt = Clock.System.now() - 3.hours
    ),
    NotificationUi(
        id = "n2",
        type = NotificationType.PAYOUT_SUCCEEDED,
        hangoutId = "h2",
        hangoutName = "Game Night",
        actorDisplayName = null,
        amountKobo = 2_400_000L,
        isRead = true,
        createdAt = Clock.System.now() - 50.hours
    )
)
