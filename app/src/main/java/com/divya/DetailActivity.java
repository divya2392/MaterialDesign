package com.divya;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailActivity extends Activity implements View.OnClickListener {

  public static final String EXTRA_PARAM_ID = "place_id";
  private ListView mList;
  private ImageView mImageView;
  private TextView mTitle;
  private LinearLayout mTitleHolder;
  private ImageButton mAddButton;
  private LinearLayout mRevealView;
  private EditText mEditTextTodo;
  private boolean isEditTextVisible;
  private InputMethodManager mInputManager;
  private Place mPlace;
  private ArrayList<String> mTodoList;
  private ArrayAdapter mToDoAdapter;
  int defaultColor;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);

    mPlace = PlaceData.placeList().get(getIntent().getIntExtra(EXTRA_PARAM_ID, 0));

    mList = (ListView) findViewById(R.id.list);
    mImageView = (ImageView) findViewById(R.id.placeImage);
    mTitle = (TextView) findViewById(R.id.textView);
    mTitleHolder = (LinearLayout) findViewById(R.id.placeNameHolder);
    mAddButton = (ImageButton) findViewById(R.id.btn_add);
    mRevealView = (LinearLayout) findViewById(R.id.llEditTextHolder);
    mEditTextTodo = (EditText) findViewById(R.id.etTodo);

    mAddButton.setOnClickListener(this);
    defaultColor = getResources().getColor(R.color.primary_dark);

    mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    mRevealView.setVisibility(View.INVISIBLE);
    isEditTextVisible = false;

    setUpAdapter();
    loadPlace();
    windowTransition();
    getPhoto();

  }

  private void setUpAdapter() {
    mTodoList = new ArrayList<>();
    mToDoAdapter = new ArrayAdapter(this, R.layout.row_todo, mTodoList);
    mList.setAdapter(mToDoAdapter);
  }

  private void loadPlace() {
    mTitle.setText(mPlace.name);
    mImageView.setImageResource(mPlace.getImageResourceId(this));
  }

  private void windowTransition() {
    getWindow().getEnterTransition().addListener(new TransitionAdapter() {
      @Override
      public void onTransitionEnd(Transition transition) {
        mAddButton.animate().alpha(1.0f);
        getWindow().getEnterTransition().removeListener(this);
      }
    });

  }

  private void addToDo(String todo) {
    mTodoList.add(todo);
  }

  private void getPhoto() {
    Bitmap photo = BitmapFactory.decodeResource(getResources(), mPlace.getImageResourceId(this));
    colorize(photo);
  }

  private void colorize(Bitmap photo) {
    Palette mPalette = Palette.generate(photo);
    applyPalette(mPalette);
  }

  private void applyPalette(Palette mPalette) {
    getWindow().setBackgroundDrawable(new ColorDrawable(mPalette.getDarkMutedColor(defaultColor)));
    mTitleHolder.setBackgroundColor(mPalette.getMutedColor(defaultColor));
    mRevealView.setBackgroundColor(mPalette.getLightVibrantColor(defaultColor));
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_add:
        // you’re declaring a field to hold a reference to the animatable that will actually perform the animation.
        Animatable mAnimatable;
        if (!isEditTextVisible) {
          revealEditText(mRevealView);
          mEditTextTodo.requestFocus();
          mInputManager.showSoftInput(mEditTextTodo, InputMethodManager.SHOW_IMPLICIT);
          mAddButton.setImageResource(R.drawable.icn_morph);
          mAnimatable = (Animatable) (mAddButton).getDrawable();
          mAnimatable.start();

        } else {
          addToDo(mEditTextTodo.getText().toString());
          mToDoAdapter.notifyDataSetChanged();
          mInputManager.hideSoftInputFromWindow(mEditTextTodo.getWindowToken(), 0);
          hideEditText(mRevealView);
          mAddButton.setImageResource(R.drawable.icon_morph_reverse);
          mAnimatable = (Animatable) (mAddButton).getDrawable();
          mAnimatable.start();

        }
    }
  }

  private void revealEditText(LinearLayout view) {

    //circular reveal.
    // The two int values are getting the x and y positions of the view with a slight offset. This offset gives the illusion that the reveal is happening from the direction of your FAB.
    //the radius gives the reveal the circular outline that you can see in the GIF above. All of these values — the x-position, y-position and the radius — you pass into the animation instance. This animation is using ViewAnimationUtils, which gives you the ability to create this circular reveal.
    int cx = view.getRight() - 30;
    int cy = view.getBottom() - 60;
    int finalRadius = Math.max(view.getWidth(), view.getHeight());
    Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
    view.setVisibility(View.VISIBLE);
    isEditTextVisible = true;
    anim.start();

  }

  // your goal is to hide the view and show the circular animation in the opposite direction. Therefore,
  // you make the initial radius the width of the view and the ending radius 0, which shrinks the circle.
  // You want to show the animation first and then hide the view.
  // To do this, you implement an animation listener and hide the view when the animation ends.
  private void hideEditText(final LinearLayout view) {
    int cx = view.getRight() - 30;
    int cy = view.getBottom() - 60;
    int initialRadius = view.getWidth();
    Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
    anim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        view.setVisibility(View.INVISIBLE);
      }
    });
    isEditTextVisible = false;
    anim.start();


  }

  @Override
  public void onBackPressed() {
    AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
    alphaAnimation.setDuration(100);
    mAddButton.startAnimation(alphaAnimation);
    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        mAddButton.setVisibility(View.GONE);
        finishAfterTransition();
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });
  }
}
