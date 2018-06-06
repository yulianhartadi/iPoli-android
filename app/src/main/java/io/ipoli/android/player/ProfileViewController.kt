package io.ipoli.android.player

import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageView
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.Pet
import io.ipoli.android.player.ProfileViewState.StateType.*
import io.ipoli.android.player.data.AndroidAvatar
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.android.synthetic.main.view_loader.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

class ProfileViewController(args: Bundle? = null) :
    ReduxViewController<ProfileAction, ProfileViewState, ProfileReducer>(args = args) {

    override val reducer = ProfileReducer

    private var isEdit: Boolean = false

    private val displayNameWatcher: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            renderDisplayNameLengthHint(s.length)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

    }

    private fun renderDisplayNameLengthHint(length: Int) {
        view!!.displayNameLengthHint.text = "$length/${Constants.DISPLAY_NAME_MAX_LENGTH}"
    }

    private fun renderBioLengthHint(length: Int) {
        view!!.bioLengthHint.text = "$length/${Constants.BIO_MAX_LENGTH}"
    }

    private val bioWatcher: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            renderBioLengthHint(s.length)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = container.inflate(R.layout.controller_profile)
        setToolbar(view.toolbar)
        toolbarTitle = stringRes(R.string.controller_profile_title)

        var coloredBackground = view.coloredBackground.background.mutate();
        coloredBackground = DrawableCompat.wrap(coloredBackground);
        DrawableCompat.setTint(coloredBackground, attrData(R.attr.colorPrimary))
        DrawableCompat.setTintMode(coloredBackground, PorterDuff.Mode.SRC_IN);

        val avatarBackground = view.playerAvatarBackground.background as GradientDrawable
        avatarBackground.setColor(attrData(android.R.attr.colorBackground))
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
        view.displayNameEdit.addTextChangedListener(displayNameWatcher)
        view.bioEdit.addTextChangedListener(bioWatcher)
    }

    override fun onDetach(view: View) {
        view.displayNameEdit.removeTextChangedListener(displayNameWatcher)
        view.bioEdit.removeTextChangedListener(bioWatcher)
        super.onDetach(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val editAction = menu.findItem(R.id.actionEdit)
        val saveAction = menu.findItem(R.id.actionSave)
        if (isEdit) {
            saveAction.isVisible = true
            editAction.isVisible = false
        } else {
            saveAction.isVisible = false
            editAction.isVisible = true
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home ->
                return router.handleBack()

            R.id.actionEdit ->
                dispatch(ProfileAction.StartEdit)

            R.id.actionSave ->
                dispatch(
                    ProfileAction.Save(
                        view!!.displayNameEdit.text.toString(),
                        view!!.bioEdit.text.toString()
                    )
                )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleBack(): Boolean {
        if(isEdit) {
            dispatch(ProfileAction.StopEdit)
            return true
        }
        return super.handleBack()
    }

    override fun onCreateLoadAction() = ProfileAction.Load

    override fun render(state: ProfileViewState, view: View) {
        when (state.type) {

            LOADING -> {
                view.profileContainer.gone()
                view.loader.visible()
            }

            PROFILE_DATA_LOADED -> {
                isEdit = false
                activity!!.invalidateOptionsMenu()
                view.loader.gone()
                view.profileContainer.visible()
                view.editGroup.gone()
                view.displayName.visible()
                view.bio.visible()
                renderAvatar(state, view)
                renderInfo(state, view)
                renderLevelProgress(state, view)
                renderPet(state, view)
                renderPetStats(state, view)
                renderPlayerStats(state, view)
            }

            EDIT -> {
                isEdit = true
                activity!!.invalidateOptionsMenu()
                renderEdit(view, state)
            }

            EDIT_STOPPED -> {
                isEdit = false
                activity!!.invalidateOptionsMenu()
                view.editGroup.gone()
                view.displayName.visible()
                view.bio.visible()
            }
        }
    }

    private fun renderEdit(view: View, state: ProfileViewState) {
        view.displayName.gone()
        view.bio.gone()
        view.editGroup.visible()
        val displayNameLength = state.displayNameText?.length ?: 0
        val bioLength = state.bioText?.length ?: 0
        view.displayNameEdit.setText(state.displayNameText ?: "")
        view.bioEdit.setText(state.bioText ?: "")
        view.displayNameEdit.setSelection(displayNameLength)
        view.bioEdit.setSelection(bioLength)

        renderDisplayNameLengthHint(displayNameLength)
        renderBioLengthHint(bioLength)
    }

    private fun renderPetStats(state: ProfileViewState, view: View) {
        view.coinBonus.text = state.coinBonus
        view.xpBonus.text = state.xpBonus
        view.itemDropBonus.text = state.itemDropBonus

        view.healthProgress.max = state.maxPetHp
        view.healthProgress.animateProgressFromZero(state.petHp)
        view.moodProgress.max = state.maxPetMp
        view.moodProgress.animateProgressFromZero(state.petMp)

        view.petStateName.text = state.petStateName
    }

    private fun renderPlayerStats(state: ProfileViewState, view: View) {
        view.playerStat1.animateToValueFromZero(state.dailyChallengeStreak)
        view.playerStat2.animateToValueFromZero(state.last7DaysAverageProductiveDuration!!.asHours.intValue)
    }

    private fun renderPet(state: ProfileViewState, view: View) {
        view.pet.setImageResource(state.petImage)
        view.petState.setImageResource(state.petStateImage)
        view.petName.text = state.pet!!.name

        val setItem: (ImageView, Int?) -> Unit = { iv, image ->
            if (image == null) iv.setImageDrawable(null)
            else iv.setImageResource(image)
        }

        setItem(view.petHat, state.petHat)
        setItem(view.petMask, state.petMask)
        setItem(view.petBody, state.petBody)

        view.pet.onDebounceClick {
            navigateFromRoot().toPet(HorizontalChangeHandler())
        }
    }

    private fun renderInfo(state: ProfileViewState, view: View) {
        view.displayName.text = state.displayNameText
        if (state.username.isNullOrBlank()) {
            view.username.gone()
        } else {
            view.username.visible()
            view.username.text = "@${state.username}"
        }
        view.info.text = state.info
        view.bio.text = state.bioText
    }

    private fun renderAvatar(state: ProfileViewState, view: View) {
        Glide.with(view.context).load(state.avatarImage)
            .apply(RequestOptions.circleCropTransform())
            .into(view.playerAvatar)
        val background = view.playerAvatar.background as GradientDrawable
        background.setColor(colorRes(AndroidAvatar.valueOf(state.avatar.name).backgroundColor))

        view.playerAvatar.onDebounceClick {
            navigateFromRoot().toAvatarStore(HorizontalChangeHandler())
        }
    }

    private fun renderLevelProgress(state: ProfileViewState, view: View) {
        view.levelProgress.max = state.levelXpMaxProgress
        view.levelProgress.animateProgressFromZero(state.levelXpProgress)
        view.levelText.text = state.levelText
        view.levelProgressText.text = state.levelProgressText
    }

    private val ProfileViewState.avatarImage
        get() = AndroidAvatar.valueOf(avatar.name).image

    private val ProfileViewState.displayNameText
        get() = if (displayName.isNullOrBlank())
            stringRes(R.string.unknown_hero)
        else
            displayName

    private val ProfileViewState.info
        get() = "$titleText | Joined $createdAgoText"

    private val ProfileViewState.titleText: String
        get() {
            val titles = resources!!.getStringArray(R.array.player_titles)
            return titles[Math.min(titleIndex, titles.size - 1)]
        }

    private val ProfileViewState.createdAgoText: String
        get() {
            val p = Period.between(createdAgo.startOfDayUTC, LocalDate.now())
            return when {
                p.isZero || p.isNegative -> stringRes(R.string.today).toLowerCase()
                p.years > 0 -> "${p.years} years ago"
                p.months > 0 -> "${p.months} months ago"
                else -> "${p.days} days ago"
            }
        }

    private val ProfileViewState.bioText
        get() =
            if (bio.isNullOrBlank())
                stringRes(R.string.blank_bio)
            else
                bio

    private val ProfileViewState.levelText
        get() = "Level $level"

    private val ProfileViewState.levelProgressText
        get() = "$levelXpProgress / $levelXpMaxProgress"

    private val ProfileViewState.petMp
        get() = pet!!.moodPoints

    private val ProfileViewState.petHp
        get() = pet!!.healthPoints

    private val ProfileViewState.maxPetMp
        get() = Pet.MAX_MP

    private val ProfileViewState.maxPetHp
        get() = Pet.MAX_HP

    private val ProfileViewState.coinBonus
        get() = "+ %.2f".format(pet!!.coinBonus) + "%"

    private val ProfileViewState.xpBonus
        get() = "+ %.2f".format(pet!!.experienceBonus) + "%"

    private val ProfileViewState.itemDropBonus
        get() = "+ %.2f".format(pet!!.itemDropBonus) + "%"

    private val ProfileViewState.petImage
        get() = AndroidPetAvatar.valueOf(pet!!.avatar.name).image

    private val ProfileViewState.petStateImage
        get() = AndroidPetAvatar.valueOf(pet!!.avatar.name).stateImage[pet.state]!!

    private val ProfileViewState.petStateName
        get() = pet!!.state.name.toLowerCase().capitalize()

    private val ProfileViewState.petHat: Int?
        get() {
            val petItems = AndroidPetAvatar.valueOf(pet!!.avatar.name).items
            return pet.equipment.hat?.let {
                petItems[it]!!
            }
        }

    private val ProfileViewState.petMask: Int?
        get() {
            val petItems = AndroidPetAvatar.valueOf(pet!!.avatar.name).items
            return pet.equipment.mask?.let {
                petItems[it]!!
            }
        }

    private val ProfileViewState.petBody: Int?
        get() {
            val petItems = AndroidPetAvatar.valueOf(pet!!.avatar.name).items
            return pet.equipment.bodyArmor?.let {
                petItems[it]!!
            }
        }
}