package com.sanny_tech.carapp.services;

import com.sanny_tech.carapp.entities.BookingRequest;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.DeleteCar;
import com.sanny_tech.carapp.entities.NewBookingRequest;
import com.sanny_tech.carapp.hire_utils.OwnerResponse;
import com.sanny_tech.carapp.review.CarReviewResponse;
import com.sanny_tech.carapp.review.RequestCarReview;
import com.sanny_tech.carapp.review.Review;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.taxi_utils.TaxiInitRequest;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface CarApiService {
    @GET("cars")
    Call<ArrayList<Car>> getCars();

    //    @Multipart
//    @POST("car/mult_upload")
//    Call<Void> newCar(@Part("car") CarRequest car, @Part List<MultipartBody.Part> files);
    @Multipart
    @POST("car/multi_upload")
    Call<Void> newCar(@Part("category") String category,
                      @Part("user_id") String admin_id, @Part("car_id") String car_id,
                      @Part("model") String model,
                      @Part("location") String location, @Part("description") String description,
                      @Part("daily_price") Double daily_price,
                      @Part("daily_down_payment") Double daily_down_payment,
                      @Part MultipartBody.Part... files);

    @POST("book")
    Call<Void> bookCar(@Body NewBookingRequest request);

    @POST("book")
    Call<Void> newBookCar(@Body NewBookingRequest request);

    @POST("book")
    Call<Void> acceptCarBook(@Body BookingRequest request);

    @POST("car/create_review")
    Call<Void> createReview(@Body Review review);

    @POST("car/review")
    Call<CarReviewResponse> getCarReview(@Body RequestCarReview review);

    @POST("search")
    Call<ArrayList<Car>> searchCars(@Body String query);

    @POST("delete_car")
    Call<Void> deleteCar(@Body DeleteCar request);

    @POST("taxi/init")
    Call<String> initCar(@Body TaxiInitRequest request);

    @Multipart
    @POST("car/multi_upload")
    Call<Void> uploadCarImages(@Part("category") String category,
                               @Part("user_id") String admin_id,
                               @Part("car_id") String car_id,
                               @Part MultipartBody.Part... files);

    @Multipart
    @POST("car/multi_upload")
    Call<Void> uploadDocs(@Part("category") String category,
                          @Part("user_id") String admin_id,
                          @Part("car_id") String car_id,
                          @Part MultipartBody.Part doc1,
                          @Part MultipartBody.Part doc2);

    @Multipart
    @POST("car/multi_upload")
    Call<Void> uploadDocs2(@Part("category") String category,
                           @Part("user_id") String admin_id,
                           @Part("car_id") String car_id,
                           @Part MultipartBody.Part doc1,
                           @Part MultipartBody.Part doc2,
                           @Part MultipartBody.Part doc3);
    @Multipart
    @POST("car/multi_upload")
    Call<Void> uploadSingleDoc(@Part("category") String category,
                           @Part("user_id") String admin_id,
                           @Part("car_id") String car_id,
                           @Part MultipartBody.Part doc1);

    @GET("taxi/images/{driver_id}")
    Call<ArrayList<String>> fetchCarImages(@Path("driver_id") String currentAccountId);
    @GET("{driver_id}/document/{status}")
    Call<ArrayList<String>> fetchDocs(@Path("driver_id")String driver_id,
                                      @Path("status")String status);
}