package com.example.da1androidnative.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.da1androidnative.R;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QrScannerFragment extends Fragment {

    // Formato esperado del token firmado: base64url(payload) + "." + base64url(HMAC)
    private static final Pattern QR_FORMAT_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");

    private long reservationId;

    private PreviewView previewView;
    private View permisoDeniedView;
    private View errorOverlay;
    private TextView errorMessageText;
    private Button btnRescan;
    private Button btnOpenSettings;

    private ProcessCameraProvider cameraProvider;
    private BarcodeScanner barcodeScanner;
    private boolean procesando = false;

    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    permisoDeniedView.setVisibility(View.GONE);
                    previewView.setVisibility(View.VISIBLE);
                    iniciarCamara();
                } else {
                    permisoDeniedView.setVisibility(View.VISIBLE);
                    previewView.setVisibility(View.GONE);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        reservationId = args != null ? args.getLong("reservationId", -1L) : -1L;

        previewView = view.findViewById(R.id.previewView);
        permisoDeniedView = view.findViewById(R.id.permisoDeniedView);
        errorOverlay = view.findViewById(R.id.errorOverlay);
        errorMessageText = view.findViewById(R.id.errorMessageText);
        btnRescan = view.findViewById(R.id.btnRescan);
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings);

        barcodeScanner = BarcodeScanning.getClient();

        btnRescan.setOnClickListener(v -> resetParaReescanear());
        btnOpenSettings.setOnClickListener(v -> permisoCamaraLauncher.launch(Manifest.permission.CAMERA));

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            permisoDeniedView.setVisibility(View.VISIBLE);
            previewView.setVisibility(View.GONE);
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void iniciarCamara() {
        com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                mostrarError("No se pudo iniciar la cámara");
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void bindCameraUseCases() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
            if (procesando) {
                imageProxy.close();
                return;
            }
            Image mediaImage = imageProxy.getImage();
            if (mediaImage == null) {
                imageProxy.close();
                return;
            }
            InputImage inputImage = InputImage.fromMediaImage(
                    mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            if (!procesando) {
                                procesando = true;
                                onQrDetectado(barcode.getRawValue());
                            }
                        }
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        });

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(
                getViewLifecycleOwner(), CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
    }

    private void onQrDetectado(@Nullable String rawValue) {
        if (rawValue == null || !QR_FORMAT_PATTERN.matcher(rawValue).matches()) {
            mostrarError("QR inválido o mal formado");
            return;
        }
        handleValidQr(rawValue);
    }


    private void handleValidQr(String qrContent) {
        // TODO: navegar a la pantalla de resultado (loading -> POST /checkin/scan
        // con CheckInScanRequest(reservationId, qrContent) -> pintar verde/rojo según respuesta).
        errorOverlay.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        procesando = false; // permite reintentar sin tocar "volver a escanear" en errores de cámara
        errorMessageText.setText(mensaje);
        errorOverlay.setVisibility(View.VISIBLE);
    }

    private void resetParaReescanear() {
        procesando = false;
        errorOverlay.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }
}