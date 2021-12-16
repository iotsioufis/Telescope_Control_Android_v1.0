package com.my_project.telescopecontrol;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;

import Jama.Matrix;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<star> star_object = new MutableLiveData<>();
    private MutableLiveData<ArrayList<AlignmentPoint>> alignmentpoints = new MutableLiveData<>();
    private MutableLiveData<ArrayList<AlignmentCentroid>> alignmentcentroids = new MutableLiveData<>();
    private MutableLiveData<ArrayList<BacklashFixesPoint>> backlashfixes = new MutableLiveData<>();
    private MutableLiveData<Integer> current_nearest_star_index = new MutableLiveData<>(0);
    private MutableLiveData<Integer> current_nearest_centroid_index = new MutableLiveData<>(0);
    private MutableLiveData<Integer> ra_offset = new MutableLiveData<>(0);
    private MutableLiveData<Integer> dec_offset = new MutableLiveData<>(0);
    private MutableLiveData<Double> alignment_time = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> goto_time = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> ra_to_goto = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> dec_to_goto = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> initial_time = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> alignment_julian_day = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> RA_micro_1 = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> DEC_micro_1 = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> HA_degrees = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> RA_decimal = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> DEC_decimal = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> DEC_decimal_transformed = new MutableLiveData<>(0.0);
    private MutableLiveData<Matrix> transformation_matrix_east = new MutableLiveData<>();
    private MutableLiveData<Matrix> transformation_matrix_west = new MutableLiveData<>();
    private MutableLiveData<String> side_of_meridian = new MutableLiveData<>("unavailable");
    private MutableLiveData<String> moving_status = new MutableLiveData<>("initially_stopped");
    private MutableLiveData<Boolean> tracking_status = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> is_first_goto = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> object_invisible = new MutableLiveData<>(false);
    private MutableLiveData<Double> latitude = new MutableLiveData<>(38.2);
    private MutableLiveData<Double> longitude = new MutableLiveData<>(21.2);
    private MutableLiveData<Boolean> is_GPS_ON = new MutableLiveData<>(false);
    private MutableLiveData<Double> sidereal_rate = new MutableLiveData<>(0.0);
    private MutableLiveData<Boolean> auto_tracking = new MutableLiveData<>(false);



    private MutableLiveData<Integer> ra_motor_steps = new MutableLiveData<>(200);
    private MutableLiveData<Integer> dec_motor_steps = new MutableLiveData<>(200);
    private MutableLiveData<Integer> ra_gear_teeth = new MutableLiveData<>(130);
    private MutableLiveData<Integer> dec_gear_teeth = new MutableLiveData<>(65);
    private MutableLiveData<Integer> ra_mount_pulley = new MutableLiveData<>(40);
    private MutableLiveData<Integer> dec_mount_pulley = new MutableLiveData<>(40);
    private MutableLiveData<Integer> ra_motor_pulley = new MutableLiveData<>(16);
    private MutableLiveData<Integer> dec_motor_pulley = new MutableLiveData<>(16);
    private MutableLiveData<Integer> ra_speed = new MutableLiveData<>(3500);
    private MutableLiveData<Integer> dec_speed = new MutableLiveData<>(2500);
    private MutableLiveData<String> config_command = new MutableLiveData<>("");
    private MutableLiveData<Boolean> energy_saving_on = new MutableLiveData<>(false);

    private MutableLiveData<Boolean> alignment_done = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> alignment_fragment_restore = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> alignment_ongoing = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> allow_extra_points = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> begin_alignment = new MutableLiveData<>(false);
    private MutableLiveData<HashMap<String, hipObject>> hipHashMap = new MutableLiveData<>();
    private MutableLiveData<Boolean> home_fragment_restore = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> is_dec_motor_inverted = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> is_ra_motor_inverted = new MutableLiveData<>(false);
    private MutableLiveData<String> one_sided_alignment = new MutableLiveData<>("");
    private MutableLiveData<Integer> ra_initial_offset_east = new MutableLiveData<>(0);
    private MutableLiveData<Integer> ra_initial_offset_west = new MutableLiveData<>(0);
    private MutableLiveData<Integer> dec_initial_offset_east = new MutableLiveData<>(0);
    private MutableLiveData<Integer> dec_initial_offset_west = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> hip_is_loading = new MutableLiveData<>(false);
    private MutableLiveData<String> jsonString_names = new MutableLiveData<>("");
    private MutableLiveData<Boolean> use_only_basic_transformation = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> include_initial_offsets = new MutableLiveData<>(false);
    private MutableLiveData<Integer> ra_goto_ending_direction = new MutableLiveData<>(0);
    private MutableLiveData<Integer> dec_goto_ending_direction = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> check_motors_direction = new MutableLiveData<>(false);
    private MutableLiveData<Integer> ra_backlash_fix = new MutableLiveData<>(0);
    private MutableLiveData<Integer> dec_backlash_fix = new MutableLiveData<>(0);
    private MutableLiveData<Integer> ra_steps_at_horizon = new MutableLiveData<>(0);
    private MutableLiveData<Double> calculated_rotation = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> degrees_to_pixel = new MutableLiveData<>(0.0);
    private MutableLiveData<Integer> dec_returned_current_position = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> camera_calibration_done = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> auto_centering_done = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> cross_move_done = new MutableLiveData<>(false);
    private MutableLiveData<Integer> cross_move_checkpoint= new MutableLiveData<>(0);




    public void setHipHashMap(HashMap<String, hipObject> hashmap) { this.hipHashMap.setValue(hashmap); }

    public LiveData<HashMap<String, hipObject>> get_HipHashMap() {
        return this.hipHashMap;
    }


    public LiveData<Boolean> get_alignment_done() {
        return alignment_done;
    }

    public void set_alignment_done(Boolean done) {
        alignment_done.setValue(done);
    }

    public LiveData<Boolean> get_alignment_fragment_restore() {
        return alignment_fragment_restore;
    }

    public void set_alignment_fragment_restore(Boolean restore_alignment) {alignment_fragment_restore.setValue(restore_alignment); }

    public LiveData<Boolean> get_home_fragment_restore() {
        return home_fragment_restore;
    }

    public void set_home_fragment_restore(Boolean restore_home) {home_fragment_restore.setValue(restore_home); }

    public LiveData<Boolean> get_begin_alignment() {
        return begin_alignment;
    }

    public void set_begin_alignment(Boolean begin) {
        begin_alignment.setValue(begin);
    }

    public LiveData<Boolean> get_allow_extra_points() {
        return allow_extra_points;
    }

    public void set_allow_extra_points(Boolean allow) {
        allow_extra_points.setValue(allow);
    }

    public LiveData<Boolean> get_alignment_ongoing() {
        return alignment_ongoing;
    }

    public void set_alignment_ongoing(Boolean is_ongoing) {alignment_ongoing.setValue(is_ongoing); }

    public LiveData<Integer> get_ra_initial_offset_east() {
        return ra_initial_offset_east;
    }

    public void set_ra_initial_offset_east(int ra_offset_e) { ra_initial_offset_east.setValue(ra_offset_e); }

    public LiveData<Integer> get_ra_initial_offset_west() {
        return ra_initial_offset_west;
    }

    public void set_ra_initial_offset_west(int ra_offset_w) {ra_initial_offset_west.setValue(ra_offset_w); }

    public LiveData<Integer> get_dec_initial_offset_east() {
        return dec_initial_offset_east;
    }

    public void set_dec_initial_offset_east(int dec_offset_e) { dec_initial_offset_east.setValue(dec_offset_e); }

    public LiveData<Integer> get_dec_initial_offset_west() {
        return dec_initial_offset_west;
    }

    public void set_dec_initial_offset_west(int dec_offset_w) { dec_initial_offset_west.setValue(dec_offset_w);  }

    public LiveData<Boolean> get_is_ra_motor_inverted() {
        return is_ra_motor_inverted;
    }

    public void set_is_ra_motor_inverted(Boolean ra_inverted) { is_ra_motor_inverted.setValue(ra_inverted); }

    public LiveData<Boolean> get_is_dec_motor_inverted() {
        return is_dec_motor_inverted;
    }

    public void set_is_dec_motor_inverted(Boolean dec_inverted) {  is_dec_motor_inverted.setValue(dec_inverted);  }

    public LiveData<String> get_one_sided_alignment() {
        return one_sided_alignment;
    }

    public void set_one_sided_alignment(String side) {
        one_sided_alignment.setValue(side);
    }

    public LiveData<Boolean> get_hip_is_loading() {
        return hip_is_loading;
    }

    public void set_hip_is_loading(Boolean loading) {
        hip_is_loading.setValue(loading);
    }

    public LiveData<String> get_jsonString_names() {
        return jsonString_names;
    }

    public void set_jsonString_names(String names) {
        jsonString_names.setValue(names);
    }


    public LiveData<Boolean> get_use_only_basic_transformation() {    return use_only_basic_transformation;   }

    public void set_use_only_basic_transformation(Boolean use_basic) {  use_only_basic_transformation.setValue(use_basic); }

    public LiveData<Boolean> get_include_initial_offsets() {
        return include_initial_offsets;
    }

    public void set_include_initial_offsets(Boolean include) {    include_initial_offsets.setValue(include);  }

    public LiveData<Boolean> get_check_motors_direction() {
        return check_motors_direction;
    }

    public void set_check_motors_direction(Boolean check) {    check_motors_direction.setValue(check);  }

    public LiveData<Boolean> get_auto_tracking() {
        return auto_tracking;
    }

    public void set_auto_tracking(Boolean tracking) {
        auto_tracking.setValue(tracking);
    }

    public LiveData<Double> get_calculated_rotation(){return calculated_rotation;}

    public void set_calculated_rotation(double  rotation) { calculated_rotation.postValue(rotation); }

    public LiveData<Double> get_degrees_to_pixel(){return degrees_to_pixel;}

    public void set_degrees_to_pixel(double  d_to_p) { degrees_to_pixel.postValue(d_to_p); }

    public LiveData<Integer> get_dec_returned_current_position() { return dec_returned_current_position; }

    public void set_dec_returned_current_position(int dec_position) { dec_returned_current_position.postValue(dec_position);  }

    public void setStar_object(star star_object1) {
        star_object.setValue(star_object1);
    }

    public void setAlignmentpoints(ArrayList<AlignmentPoint> points) { alignmentpoints.setValue(points);  }
    public void setBacklashfixes(ArrayList<BacklashFixesPoint> point_fixes) { backlashfixes.setValue(point_fixes);  }

    public void setAlignmentcentroids(ArrayList<AlignmentCentroid> centroids) {alignmentcentroids.setValue(centroids); }

    public void set_current_nearest_star_index(int index) {current_nearest_star_index.setValue(index); }

    public void set_current_nearest_centroid_index(int index) {current_nearest_centroid_index.setValue(index); }

    public void setRA_offset(int transmitted_ra_offest) { ra_offset.setValue(transmitted_ra_offest); }

    public void setDEC_offset(int transmitted_dec_offest) { dec_offset.setValue(transmitted_dec_offest);  }

    public void setAlignment_time(double decimal_time) {
        alignment_time.setValue(decimal_time);
    }

    public void setRA_to_goto(double ra) {
        ra_to_goto.setValue(ra);
    }

    public void setDEC_to_goto(double dec) {
        dec_to_goto.setValue(dec);
    }

    public void setGoto_time(double decimal_goto_time) {
        goto_time.setValue(decimal_goto_time);
    }

    public void setInitial_time(double decimal_intial_time) { initial_time.setValue(decimal_intial_time); }

    public void setAlignment_julian_day(double day) {
        alignment_julian_day.setValue(day);
    }

    public void setRA_micro_1(double RA_micro) {
        RA_micro_1.setValue(RA_micro);
    }

    public void setDEC_micro_1(double DEC_micro) {
        DEC_micro_1.setValue(DEC_micro);
    }

    public void setRA_decimal(double ra_after_precession) { RA_decimal.setValue(ra_after_precession); }

    public void setDEC_decimal(double dec_after_precession) {DEC_decimal.setValue(dec_after_precession); }

    public void setDEC_decimal_transformed(double dec_after_transformation) {DEC_decimal_transformed.setValue(dec_after_transformation); }

    public LiveData<Integer> get_ra_goto_ending_direction() {
        return ra_goto_ending_direction;
    }

    public void set_ra_goto_ending_direction(int ra_dir) { ra_goto_ending_direction.setValue(ra_dir); }

    public LiveData<Integer> get_dec_goto_ending_direction() {
        return dec_goto_ending_direction;
    }

    public void set_dec_goto_ending_direction(int dec_dir) { dec_goto_ending_direction.setValue(dec_dir); }

    public LiveData<Integer> get_ra_backlash_fix() {
        return ra_backlash_fix;
    }
    public void set_ra_backlash_fix(int ra_fix) { ra_backlash_fix.setValue(ra_fix); }

    public LiveData<Integer> get_dec_backlash_fix() {
        return dec_backlash_fix;
    }
    public void set_dec_backlash_fix(int dec_fix) { dec_backlash_fix.setValue(dec_fix); }

    public LiveData<Integer> get_ra_steps_at_horizon() {
        return ra_steps_at_horizon;
    }
    public void set_ra_steps_at_horizon(int ra_step_zero_altitude) { ra_steps_at_horizon.setValue(ra_step_zero_altitude); }

    public LiveData<Boolean> get_camera_calibration_done() {
        return camera_calibration_done;
    }
    public void set_camera_calibration_done(Boolean calibration_done) {camera_calibration_done.setValue(calibration_done);}

    public LiveData<Boolean> get_auto_centering_done() { return auto_centering_done;}
    public void set_auto_centering_done(Boolean centering_done) {auto_centering_done.setValue(centering_done);}

    public LiveData<Boolean> get_cross_move_done() { return cross_move_done;}
    public void set_cross_move_done(Boolean move_done) {cross_move_done.setValue(move_done);}

    public LiveData<Integer> get_cross_move_checkpoint() {
        return cross_move_checkpoint;
    }
    public void set_cross_move_checkpoint(int checkpoint_num) { cross_move_checkpoint.setValue(checkpoint_num); }



    public void setHA_degrees(double calculated_HA) {
        HA_degrees.setValue(calculated_HA);
    }

    public void setSide_of_meridian(String side) {
        side_of_meridian.setValue(side);
    }

    public void set_sidereal_rate(double rate) {
        sidereal_rate.setValue(rate);
    }

    public void setTransformation_matrix_east(Matrix matrix_east) {
        transformation_matrix_east.setValue(matrix_east);
    }

    public void setTransformation_matrix_west(Matrix matrix_west) { transformation_matrix_west.setValue(matrix_west); }

    public void setMoving_status(String status) {
        moving_status.setValue(status);
    }

    public void setTracking_status(Boolean tracking) {
        tracking_status.setValue(tracking);
    }

    public void set_first_goto(Boolean is_first) {
        is_first_goto.setValue(is_first);
    }

    public void set_object_invisible(Boolean is_invisible) {
        object_invisible.setValue(is_invisible);
    }

    public void setLatitude(double lat) {
        latitude.setValue(lat);
    }

    public void setLongitude(double lon) {
        longitude.setValue(lon);
    }

    public void set_is_GPS_ON(Boolean gps_state) {
        is_GPS_ON.setValue(gps_state);
    }

    public void setRA_motor_steps(int ra_steps) {
        ra_motor_steps.setValue(ra_steps);
    }

    public void setDEC_motor_steps(int dec_steps) {
        dec_motor_steps.setValue(dec_steps);
    }

    public void setRA_gear_teeth(int ra_gear_t) {
        ra_gear_teeth.setValue(ra_gear_t);
    }

    public void setDEC_gear_teeth(int dec_gear_t) {
        dec_gear_teeth.setValue(dec_gear_t);
    }

    public void setRA_mount_pulley(int ra_mount_p) {
        ra_mount_pulley.setValue(ra_mount_p);
    }

    public void setDEC_mount_pulley(int dec_mount_p) {
        dec_mount_pulley.setValue(dec_mount_p);
    }

    public void setRA_motor_pulley(int ra_motor_p) {
        ra_motor_pulley.setValue(ra_motor_p);
    }

    public void setDEC_motor_pulley(int dec_motor_p) {
        dec_motor_pulley.setValue(dec_motor_p);
    }

    public void setRASpeed(int ra_sp) {
        ra_speed.setValue(ra_sp);
    }

    public void setDECSpeed(int dec_sp) {
        dec_speed.setValue(dec_sp);
    }

    public void setConfig_command(String command) {
        config_command.setValue(command);
    }

    public void set_energy_saving_on(Boolean energy_saving) {energy_saving_on.setValue(energy_saving);}


    public LiveData<star> getStar_object() {
        return star_object;
    }

    public LiveData<ArrayList<AlignmentPoint>> getAlignmentpoints() {
        return alignmentpoints;
    }
    public LiveData<ArrayList<BacklashFixesPoint>> getBacklashfixes() {
        return backlashfixes;
    }

    public LiveData<ArrayList<AlignmentCentroid>> getAlignmentcentroids() {return alignmentcentroids;}

    public LiveData<Integer> get_current_nearest_star_index() {
        return current_nearest_star_index;
    }

    public LiveData<Integer> get_current_nearest_centroid_index() {return current_nearest_centroid_index; }


    public LiveData<Integer> getRA_offset() {
        return ra_offset;
    }

    public LiveData<Integer> getDEC_offset() {
        return dec_offset;
    }

    public LiveData<Double> getAlignment_time() {
        return alignment_time;
    }

    public LiveData<Double> getGoto_time() {
        return goto_time;
    }

    public LiveData<Double> getRA_to_goto() {
        return ra_to_goto;
    }

    public LiveData<Double> getDEC_to_goto() {
        return dec_to_goto;
    }

    public LiveData<Double> getInitial_time() {
        return initial_time;
    }

    public LiveData<Double> getAlignment_julianday() {
        return alignment_julian_day;
    }

    public LiveData<Double> getRA_micro_1() {
        return RA_micro_1;
    }

    public LiveData<Double> getDEC_micro_1() {
        return DEC_micro_1;
    }

    public LiveData<Double> getRA_decimal() {
        return RA_decimal;
    }

    public LiveData<Double> getDEC_decimal() {
        return DEC_decimal;
    }

    public LiveData<Double> getDEC_decimal_transformed() {
        return DEC_decimal_transformed;
    }


    public LiveData<Double> getHA_degrees() {
        return HA_degrees;
    }

    public LiveData<Double> get_sidereal_rate() {
        return sidereal_rate;
    }

    public LiveData<String> getSide_of_meridian() {
        return side_of_meridian;
    }

    public LiveData<Matrix> getTransformation_matrix_east() {
        return transformation_matrix_east;
    }

    public LiveData<Matrix> getTransformation_matrix_west() {
        return transformation_matrix_west;
    }

    public LiveData<String> getMoving_status() {
        return moving_status;
    }

    public LiveData<Boolean> getTracking_status() {
        return tracking_status;
    }

    public LiveData<Boolean> get_is_first_goto() {
        return is_first_goto;
    }

    public LiveData<Boolean> get_object_invisible() {
        return object_invisible;
    }

    public LiveData<Double> getLatitute() {
        return latitude;
    }

    public LiveData<Double> getLongitude() {
        return longitude;
    }


    public LiveData<Boolean> get_is_GPS_ON() {
        return is_GPS_ON;
    }

    public LiveData<Integer> getRA_motor_steps() {
        return ra_motor_steps;
    }

    public LiveData<Integer> getDEC_motor_steps() {
        return dec_motor_steps;
    }

    public LiveData<Integer> getRA_gear_teeth() {
        return ra_gear_teeth;
    }

    public LiveData<Integer> getDEC_gear_teeth() {
        return dec_gear_teeth;
    }

    public LiveData<Integer> getRA_mount_pulley() {
        return ra_mount_pulley;
    }

    public LiveData<Integer> getDEC_mount_pulley() {
        return dec_mount_pulley;
    }

    public LiveData<Integer> getRA_motor_pulley() {
        return ra_motor_pulley;
    }

    public LiveData<Integer> getDEC_motor_pulley() {
        return dec_motor_pulley;
    }

    public LiveData<Integer> getRASpeed() {
        return ra_speed;
    }

    public LiveData<Integer> getDECSpeed() {
        return dec_speed;
    }

    public LiveData<String> getConfig_command() {
        return config_command;
    }

    public LiveData<Boolean> get_energy_saving_on() {
        return energy_saving_on;
    }


}
