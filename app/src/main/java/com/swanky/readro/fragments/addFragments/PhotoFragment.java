package com.swanky.readro.fragments.addFragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.swanky.readro.R;
import com.swanky.readro.databinding.FragmentPhotoBinding;

public class PhotoFragment extends Fragment {

    private FragmentPhotoBinding binding;
    private ActivityResultLauncher<Intent> galleryIntentLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryPermissionLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private Bitmap imageBitmap;
    private MyPhotoListener listener;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        registerLauncher();
        binding.setImageButton.setOnClickListener(this::goToCameraOrPickImage);
    }

    @SuppressLint("SetTextI18n")
    private void goToCameraOrPickImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.DialogTheme);
        if (getActivity() != null) {
            @SuppressLint("InflateParams") View dialogView = getActivity().getLayoutInflater().inflate(R.layout.layout_warning_dailog, null);
            builder.setView(dialogView);

            ImageView alertIcon = dialogView.findViewById(R.id.alertIcon);
            Button buttonCamera = dialogView.findViewById(R.id.buttonNegative);
            Button buttonGallery = dialogView.findViewById(R.id.buttonPositive);
            TextView alertTitle = dialogView.findViewById(R.id.alertTitleTxt);
            TextView alertMessage = dialogView.findViewById(R.id.alertMessageTxt);

            alertIcon.setImageResource(R.drawable.ico_pick_image);
            alertTitle.setText("Select photo");
            alertMessage.setText("You can choose a photo for your book from the gallery or take it from the camera.");
            buttonCamera.setText("Camera");
            buttonGallery.setText("Gallery");

            final AlertDialog alertDialog = builder.create();

            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            buttonCamera.setOnClickListener(view1 -> {
                goToCamera(view1);
                alertDialog.cancel();
            });

            buttonGallery.setOnClickListener(view1 -> {
                goToGallery(view1);
                alertDialog.cancel();
            });

            alertDialog.show();
        }
    }

    private void goToCamera(View view) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Snackbar.make(view, R.string.permission_explanation_camera, Snackbar.LENGTH_INDEFINITE).setAction(R.string.permission_button, view1 -> {
                    //Request permission
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }).show();
            } else {
                //Request Permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        } else {
            //Permission is granted. You can go to the camera
            cameraLauncher.launch(null);
        }
    }

    private void goToGallery(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //For android version 33+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, getString(R.string.permission_explanation), Snackbar.LENGTH_INDEFINITE).setAction(R.string.permission_button, view1 -> {
                        //Request Permission
                        galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    }).show();
                } else {
                    //Request permission
                    galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                //Go to gallery
                Intent goToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntentLauncher.launch(goToGallery);
            }
        } else {
            //For android version until 33
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, getString(R.string.permission_explanation), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.permission_button), view1 -> {
                        //Request Permission
                        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }).show();
                } else {
                    //Request permission
                    galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                //Permission is granted goTo Gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntentLauncher.launch(intentToGallery);
            }
        }
    }

    private void registerLauncher() {
        galleryPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntentLauncher.launch(intentToGallery);
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_explanation), Toast.LENGTH_SHORT).show();
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                cameraLauncher.launch(null);
            } else {
                Toast.makeText(requireContext(), R.string.permission_explanation_camera, Toast.LENGTH_SHORT).show();
            }
        });

        galleryIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result.getResultCode() == RESULT_OK) {
                Intent intentFromResult = result.getData();
                if (intentFromResult != null) {
                    Uri imageData = intentFromResult.getData();
                    //Convert the bitmap
                    try {

                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), imageData);
                            imageBitmap = makeSmallerImage(ImageDecoder.decodeBitmap(source), 300);
                            binding.selfLoadImage.setImageBitmap(imageBitmap);
                        } else {
                            imageBitmap = makeSmallerImage(MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageData), 300);
                            binding.selfLoadImage.setImageBitmap(imageBitmap);
                        }

                        listener.chosenPhoto(imageBitmap);

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), R.string.galleryIntentErrorMessage, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(requireContext(), R.string.galleryIntentErrorMessage, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), R.string.galleryIntentErrorMessage, Toast.LENGTH_SHORT).show();
            }
        });


        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null) {
                binding.selfLoadImage.setImageBitmap(result);
                imageBitmap = makeSmallerImage(result, 300);
                listener.chosenPhoto(imageBitmap);
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_camera), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public interface MyPhotoListener {
        void chosenPhoto(Bitmap chosenPhotoData);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CategoryFragment.MyCategoryListener) {
            listener = (MyPhotoListener) context;
        } else {
            throw new RuntimeException(context + " must implement MyFragmentListener");
        }
    }

}