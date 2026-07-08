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
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.CheckInScanRequest;
import com.example.da1androidnative.data.model.CheckInScanResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkUtils;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class QrScannerFragment extends Fragment {

    // Formato esperado del token firmado: base64url(payload) + "." + base64url(HMAC)
    private static final Pattern QR_FORMAT_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");

    @Inject ApiService apiService;

    private long reservationId;

    private PreviewView previewView;
    private View permisoDeniedView;
    private View loadingOverlay;
    private View successOverlay;
    private View errorOverlay;
    private TextView successMessageText;
    private TextView errorMessageText;
    private Button btnBackToVoucher;
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
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        successOverlay = view.findViewById(R.id.successOverlay);
        errorOverlay = view.findViewById(R.id.errorOverlay);
        successMessageText = view.findViewById(R.id.successMessageText);
        errorMessageText = view.findViewById(R.id.errorMessageText);
        btnBackToVoucher = view.findViewById(R.id.btnBackToVoucher);
        btnRescan = view.findViewById(R.id.btnRescan);
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings);

        barcodeScanner = BarcodeScanning.getClient();

        btnBackToVoucher.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
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
        if (reservationId == -1L) {
            mostrarError("No se encontró la reserva para confirmar asistencia");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            mostrarError("Sin conexión. Conectate a internet para confirmar asistencia.");
            return;
        }

        mostrarCargando();
        apiService.scanCheckIn(new CheckInScanRequest(reservationId, qrContent))
                .enqueue(new Callback<CheckInScanResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CheckInScanResponse> call,
                                           @NonNull Response<CheckInScanResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            bindScanResponse(response.body());
                        } else {
                            mostrarError(extractErrorMessage(
                                    response.errorBody(),
                                    "QR inválido o no corresponde a esta reserva"
                            ));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CheckInScanResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        mostrarError("No se pudo confirmar la asistencia. Intentá nuevamente.");
                    }
                });
    }

    private void bindScanResponse(CheckInScanResponse response) {
        if ("CONFIRMED".equalsIgnoreCase(response.getStatus())) {
            String message = valueOrFallback(response.getMessage(), "Asistencia confirmada");
            String activityName = response.getActivityName();
            if (activityName != null && !activityName.trim().isEmpty()) {
                message = message + "\n" + activityName;
            }
            mostrarConfirmacion(message);
        } else {
            mostrarError(valueOrFallback(response.getMessage(), "No se pudo confirmar la asistencia"));
        }
    }

    private void mostrarCargando() {
        errorOverlay.setVisibility(View.GONE);
        successOverlay.setVisibility(View.GONE);
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void mostrarConfirmacion(String mensaje) {
        procesando = true;
        loadingOverlay.setVisibility(View.GONE);
        errorOverlay.setVisibility(View.GONE);
        successMessageText.setText(mensaje);
        successOverlay.setVisibility(View.VISIBLE);
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    private void mostrarError(String mensaje) {
        procesando = false; // permite reintentar sin tocar "volver a escanear" en errores de cámara
        loadingOverlay.setVisibility(View.GONE);
        successOverlay.setVisibility(View.GONE);
        errorMessageText.setText(mensaje);
        errorOverlay.setVisibility(View.VISIBLE);
    }

    private void resetParaReescanear() {
        procesando = false;
        loadingOverlay.setVisibility(View.GONE);
        successOverlay.setVisibility(View.GONE);
        errorOverlay.setVisibility(View.GONE);
    }

    private String extractErrorMessage(ResponseBody errorBody, String fallback) {
        if (errorBody == null) return fallback;

        try {
            String rawBody = errorBody.string();
            if (rawBody == null || rawBody.trim().isEmpty()) return fallback;

            JSONObject json = new JSONObject(rawBody);
            String detail = json.optString("detail");
            if (detail != null && !detail.trim().isEmpty()) return detail;

            String message = json.optString("message");
            if (message != null && !message.trim().isEmpty()) return message;

            String error = json.optString("error");
            if (error != null && !error.trim().isEmpty()) return error;

            return fallback;
        } catch (IOException | JSONException | RuntimeException e) {
            return fallback;
        }
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
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
