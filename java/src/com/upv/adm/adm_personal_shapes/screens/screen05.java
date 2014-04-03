package com.upv.adm.adm_personal_shapes.screens;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.upv.adm.adm_personal_shapes.R;
import com.upv.adm.adm_personal_shapes.classes.BeanShape;
import com.upv.adm.adm_personal_shapes.classes.CustomActionBarActivity;
import com.upv.adm.adm_personal_shapes.classes.CustomListItem;
import com.upv.adm.adm_personal_shapes.classes.GlobalContext;
import com.upv.adm.adm_personal_shapes.classes.SQLite;
import com.upv.adm.adm_personal_shapes.classes.Utils;
import com.upv.adm.adm_personal_shapes.classes.WebServerProxy;

public class screen05 extends CustomActionBarActivity {
	
	private TextView textview_selectshape;
	private EditText 
			edittext_name,
			edittext_description;

	private Long id;
	
	private CustomListItem[] 
			typesListviewItems;
			
	private static final int DIALOG_ALERT = 10;
	
	private String 
			resizedImagePath,
			coords;

	private ArrayList<BeanShape> list_places = new ArrayList<BeanShape>();
	private ArrayList<BeanShape> list_plots = new ArrayList<BeanShape>();

	private BeanShape 
				place,
				plot;

	private Object[] typesData;
	private Spinner spinner_types;
		
	private ImageButton 
			button_share, 
			button_save, 
			button_delete;
	
	private Button 
			button_fillqr,
			button_map,
			button_coordinates;
	
	private RadioGroup radiogroup_shape;
	
	private RadioButton
			radiobutton_place,
			radiobutton_plot;
	
	private View layout_spinnertypes;
	
	private ImageView image_photo;
	
	// keep track of camera capture intent
	final int CAMERA_CAPTURE = 1;
	// keep track of cropping intent
	final int PIC_CROP = 2;
	// captured picture uri
	private Uri picUri;
	
	private ProgressDialog pd;
	
	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.screen05);
		GlobalContext.setContext(getApplicationContext());
		SQLite.staticInitialization();

		initControls();
		if (GlobalContext.shape_id != null) {
			fillFields();
		}
		
	}
	
	public void fillFields () {
		if (GlobalContext.shape_id != null) {
			ArrayList<BeanShape> shapes = (ArrayList<BeanShape>)SQLite.getShapes("WHERE id = " + GlobalContext.shape_id);
			if (shapes.size() == 1) {
				BeanShape shape = shapes.get(0);
				edittext_name.setText(shape.getName());
				edittext_description.setText(shape.getDescription());	
				Utils.setSelectedKey(spinner_types, shape.getType());
				coords = screen21.coords; 
				
				//Pendiente por hacer, ya que la foto es temporal, falta guardarla con el movil.
				//image_photo.setImag(shape.getPhoto());
			}
		}
	}
	
	public void initControls(){
	
		textview_selectshape = (TextView) findViewById(R.id.textview_selectshape);
		radiogroup_shape = (RadioGroup) findViewById(R.id.radiogroup_shape);
		
		radiobutton_place = (RadioButton) findViewById(R.id.radiobutton_place);
		radiobutton_plot = (RadioButton) findViewById(R.id.radiobutton_plot);
		button_fillqr = (Button) findViewById(R.id.button_fillqr);

		layout_spinnertypes = (View) findViewById(R.id.layout_spinnertypes);
		spinner_types = (Spinner) findViewById(R.id.spinner_types);

		edittext_name = (EditText) findViewById(R.id.edittext_placename);
		edittext_description = (EditText) findViewById(R.id.edittext_description);
		image_photo = (ImageView) findViewById(R.id.imageview_photo);
		
		button_map = (Button) findViewById(R.id.button_map);
		button_coordinates = (Button) findViewById(R.id.button_coordinates);
		button_share = (ImageButton) findViewById(R.id.button_send);
		button_save = (ImageButton) findViewById(R.id.button_save);
		button_delete = (ImageButton) findViewById(R.id.button_delete);
		
		image_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (v.getId() == R.id.imageview_photo) {
					try {
						selectImage();
					} catch (ActivityNotFoundException anfe) {
						@SuppressWarnings("unused")
						String errorMessage = "Whoops - your device doesn't support capturing images!";
					}
				}
			}
		});

		typesListviewItems = GlobalContext.getTypesData();
		ArrayAdapter<CustomListItem> arrayAdapter_genders = new ArrayAdapter<CustomListItem>(this, android.R.layout.simple_spinner_dropdown_item, typesListviewItems);
		spinner_types.setAdapter(arrayAdapter_genders);
		
		button_fillqr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImage();
				//Uri:  resizedImagePath
			}
		});

		if (GlobalContext.shape_id == null) {
			((ViewManager) button_share.getParent()).removeView(button_share);
			((ViewManager) button_delete.getParent()).removeView(button_delete);

		}
		else
			((ViewManager) radiogroup_shape.getParent()).removeView(radiogroup_shape);
		
		if(screen04.select_plot == true){
			((ViewManager) layout_spinnertypes.getParent()).removeView(layout_spinnertypes);
			((ViewManager) button_fillqr.getParent()).removeView(button_fillqr);
			textview_selectshape.setText("Parcela");
			screen04.select_plot = false;
		}

		button_map.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapClick();
			}
		});
		
		button_coordinates.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				coordinatesClick();
			}
		});
		
		button_share.setOnClickListener(new OnClickListener() {  @Override  public void onClick(View v) {
			AlertDialog.Builder builder_share =new AlertDialog.Builder(screen05.this);
			builder_share.setTitle("Compartir con...");
			builder_share.setNeutralButton("Personal Shapes",new DialogInterface.OnClickListener() {  
				@Override  
				public void onClick(DialogInterface dialog, int which) {
					shareClick();
				}  
			});  
			builder_share.setPositiveButton("Redes Sociales",new DialogInterface.OnClickListener() {  
				@Override  
				public void onClick(DialogInterface dialog, int which) {
						
				
				}  
			}); 
			builder_share.show();  
			}  
		});
		
		button_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveClick();
			}
		});
		
		radiogroup_shape.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radiobutton_place) {
					textview_selectshape.setText("Lugar personal");
				    (findViewById(R.id.layout_spinnertypes)).setVisibility(View.VISIBLE);
				    (findViewById(R.id.layout_fillqr)).setVisibility(View.VISIBLE);
				}
				else if (checkedId == R.id.radiobutton_plot) {
					textview_selectshape.setText("Parcela");
				    (findViewById(R.id.layout_spinnertypes)).setVisibility(View.GONE);
				    (findViewById(R.id.layout_fillqr)).setVisibility(View.GONE);
				}
			}
		});
	}
	
	
	public void saveClick() {
		String name = edittext_name.getText().toString();
		String description = edittext_description.getText().toString();
		String coords = edittext_description.getText().toString();
		if (radiobutton_place.isChecked()) {
			String type = Utils.getSelectedKey(spinner_types);
			BeanShape place = new BeanShape(id, name, description, type, coords, resizedImagePath);
			long result = SQLite.saveShape(place);
			System.out.println("Result: " + result);
		}
		else {
			BeanShape plot = new BeanShape(id, name, description, null, coords, resizedImagePath);
			SQLite.saveShape(plot);
		}
		if(id == null) {startActivity(new Intent(getApplicationContext(), screen03.class));}
	}
	
	public void coordinatesClick() {
		
		startActivity(new Intent(getApplicationContext(), screen21.class));
		}
	
	public void mapClick() {
		
		String name = edittext_name.getText().toString();
		String description = edittext_description.getText().toString();
		String coords = screen21.coords;
		
		if (radiobutton_place.isChecked()) {
			
			String type = Utils.getSelectedKey(spinner_types);
			BeanShape place = new BeanShape(id, name, description, type, coords, resizedImagePath);
			long result = SQLite.saveShape(place);
			System.out.println("Result: " + result);
		}
		else {
			//UtilsAddBeanPlotToMap(BeanShape plot, WebView webview)
			BeanShape plot = new BeanShape(id, name, description, null, coords, resizedImagePath);
		}
		Intent in = new Intent(getApplicationContext(),	screen22.class);
		
		//in.putExtra("plot", plot);

		startActivity(in);
		
		}

	public void UtilsAddBeanPlotToMap(BeanShape plot, WebView webview_map){
		
	}
	public void selectImage(View v) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, 0);
	}

	private void selectImage() {

		final CharSequence[] options = { "C�mara", "Galeria", "Cancelar" };
		
		AlertDialog.Builder builder = new AlertDialog.Builder(screen05.this);
		builder.setTitle("Selecciona una opci�n");
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (options[item].equals("C�mara")) {
					// use standard intent to capture an image
					Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					// we will handle the returned data in onActivityResult
					startActivityForResult(captureIntent, CAMERA_CAPTURE);
				} else if (options[item].equals("Galeria")) {
					// acci�n para buscar una imagen en la galeria
					Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(pickPhoto, 1);// one can be replced with any action code
				} else if (options[item].equals("Cancelar")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			// user is returning from capturing an image using the camera
			if (requestCode == CAMERA_CAPTURE) {
				// get the Uri for the captured image
				picUri = data.getData();
				// carry out the crop operation
				performCrop();
			}
			// user is returning from cropping the image
			else if (requestCode == PIC_CROP) {
				// get the returned data
				Bundle extras = data.getExtras();
				// get the cropped bitmap
				Bitmap thePic = extras.getParcelable("data");
				
				// display the returned cropped image

				Bitmap thePicResized = Bitmap.createScaledBitmap(thePic, 512,512, false);
				image_photo.setImageBitmap(thePicResized);

				try {
					File tempFile = File.createTempFile("temp_file_",".jpg");
					FileOutputStream fos = new FileOutputStream(tempFile);
					thePicResized.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					resizedImagePath = tempFile.getPath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void performCrop() {
		// take care of exceptions
		try {
			// call the standard crop action intent (the user device may not support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 512);
			cropIntent.putExtra("outputY", 512);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP);
		}
		// respond to users whose devices do not support the crop action
		catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void shareClick(){
		
		String name = edittext_name.getText().toString();
		String type = Utils.getSelectedKey(spinner_types);
		String description = edittext_description.getText().toString();

		Hashtable<String, String> data = new Hashtable<String, String>();
		data.put("name", name);
		data.put("description", description);
		data.put("type",  type);
		
		
		if (resizedImagePath != null)
			data.put("photo", resizedImagePath);
		
		(new AsyncTask<Hashtable<String, String>, Void, String[]>() {
			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(getCurrentActivity());
				pd.setTitle("Comunicando con el servidor");
				pd.setMessage("Por favor, espere mientras se comparte...");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}
			@Override
			protected String[] doInBackground(Hashtable<String, String>... data) {
				String[] result = WebServerProxy.register_user(data[0]);  //share_shape
				
				return result;
			}
			@Override
			protected void onPostExecute(String[] result) {
				if (pd!=null)
					pd.dismiss();
				if (result[0].equals("success")) {
					// Respuesta correcta por parte del servidor, entonces seguimos adelante
					// esta activity no se necesitar� m�s, por tanto la cerramos
					getCurrentActivity().finish(); 
					startActivity(new Intent(getApplicationContext(), screen04.class));
				}
				else {
				    AlertDialog ad = new AlertDialog.Builder(getCurrentActivity()).create();  
				    ad.setCancelable(false);  
				    ad.setMessage(
				    		"No ha sido posible compartir. El servidor ha devuelto el siguiente c�digo de error:\n\n" +
				    		result[1]
				    );  
				    ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {  
				        @Override  
				        public void onClick(DialogInterface dialog, int which) {  
				            dialog.dismiss();                      
				        }  
				    });  
				    ad.show();  
				}
			}
		}).execute(data);
		
	}
		
		
	}

