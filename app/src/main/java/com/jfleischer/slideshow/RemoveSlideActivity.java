package com.jfleischer.slideshow;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jfleischer.slideshow.models.FileActivityMode;
import com.jfleischer.slideshow.models.FileActivityPurpose;
import com.jfleischer.slideshow.models.FileItem;
import com.jfleischer.slideshow.models.Slide;
import com.jfleischer.slideshow.views.FileAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class RemoveSlideActivity extends AppCompatActivity {
    static public final String PICK_KEY_FILE = "PICK_KEY_FILE";
    static private File mDirectory;
    static private Map<String, Integer> mPositions = new HashMap<>();
    private File         mParent;
    private File []      mDirs;
    private File []      mFiles;
    private Handler mHandler;
    private Runnable     mUpdateFiles;
    private FileAdapter adapter;

    private ListView listView;
    private FileActivityPurpose mPurpose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_slide);

        mPurpose = PICK_KEY_FILE.equals(getIntent().getAction()) ? FileActivityPurpose.PickedDirectory : FileActivityPurpose.PickedFile;

        final String storageState = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(storageState)
                && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Storage media not present");
            builder.setMessage("Sharing the storage media with a PC can make it inaccessible");
            final AlertDialog alert = builder.create();
            alert.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.show();
            return;
        }

        //update(mode);

        if (mDirectory == null) {
            mDirectory = new File(getFilesDir(), SlideShowActivity.STORAGE_IMG_DIR);
        }
        // Create a list adapter...
        listView = findViewById(R.id.list_view);
        adapter = new FileAdapter(getLayoutInflater(), FileActivityMode.Delete);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(listView,view,position,id);
            }
        });

        // ...that is updated dynamically when files are scanned
        mHandler = new Handler();
        mUpdateFiles = new Runnable() {
            public void run() {

                mParent = mDirectory.getParentFile();

                mDirs = mDirectory.listFiles(new FileFilter() {

                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });
                if (mDirs == null)
                    mDirs = new File[0];

                mFiles = mDirectory.listFiles(new FileFilter() {

                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return false;
                        final String fileName = file.getName().toLowerCase();
                        switch (mPurpose) {
                            case PickedFile:
                                // accept only these files
                                return isValidFileName(fileName);
                            case PickedDirectory:
                                return fileName.endsWith(".pfx");
                            default:
                                return false;
                        }
                    }
                });
                if (mFiles == null)
                    mFiles = new File[0];

                Arrays.sort(mFiles, new Comparator<File>() {
                    public int compare(File arg0, File arg1) {
                        return arg0.getName().compareToIgnoreCase(arg1.getName());
                    }
                });

                Arrays.sort(mDirs, new Comparator<File>() {
                    public int compare(File arg0, File arg1) {
                        return arg0.getName().compareToIgnoreCase(arg1.getName());
                    }
                });

                adapter.clear();
                mParent = null;

                for (final File f : mFiles)
                    adapter.add(new FileItem(FileItem.Type.DOC, f.getName()));

                lastPosition();
            }
        };

        // Start initial file scan...
        mHandler.post(mUpdateFiles);

        // ...and observe the directory and scan files upon changes.
        final FileObserver observer = new FileObserver(mDirectory.getPath(), FileObserver.CREATE | FileObserver.DELETE) {
            public void onEvent(int event, String path) {
                mHandler.post(mUpdateFiles);
            }
        };
        observer.startWatching();
        update();
    }

    private boolean isValidFileName(String fileName){
        return Slide.isValidFileName(fileName);
    }

    private void lastPosition() {
        final String p = mDirectory.getAbsolutePath();
        if (mPositions.containsKey(p))
            listView.setSelection(mPositions.get(p));
    }


    protected void onListItemClick(ListView l, View v, int position, long id) {



        mPositions.put(mDirectory.getAbsolutePath(), listView.getFirstVisiblePosition());

        if (position < (mParent == null ? 0 : 1)) {
            mDirectory = mParent;
            mHandler.post(mUpdateFiles);
            return;
        }
        position -= (mParent == null ? 0 : 1);

        if (position < mDirs.length) {
            mDirectory = mDirs[position];
            mHandler.post(mUpdateFiles);
            return;
        }
        position -= mDirs.length;

        final Uri uri = Uri.parse(mFiles[position].getAbsolutePath());
        final Intent intent = new Intent(this,SlideShowActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        switch (mPurpose) {
            case PickedFile:
                //startActivity(intent);
                String file_name = mFiles[position].getName();
                if(new File(mFiles[position].getAbsolutePath()).delete()) {
                    mHandler.post(mUpdateFiles);
                    listView.invalidate();
                    File storageDirectory = new File(getFilesDir(), SlideShowActivity.STORAGE_IMG_DIR);
                    if (storageDirectory.list().length == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setCancelable(false);
                        builder.setTitle(getString(R.string.all_slides_removed));
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                update();
                            }
                        });
                        builder.show();
                    } else {
                        Toast.makeText(this, getString(R.string.toast_remove, file_name), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.toast_error,file_name), Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK, intent);
                break;
            case PickedDirectory:
                break;
        }
    }

    private void update(){
        adapter = new FileAdapter(getLayoutInflater(), FileActivityMode.Delete);
        listView.setAdapter(adapter);
        mDirectory = new File(getFilesDir(), SlideShowActivity.STORAGE_IMG_DIR);
        mHandler.post(mUpdateFiles);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDirectory != null)
            mPositions.put(mDirectory.getAbsolutePath(), listView.getFirstVisiblePosition());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slide_show_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent = null;
        if (id == R.id.action_options) {
            intent = new Intent(this, SlideShowOptionsActivity.class);
        } else if (id == R.id.action_add) {
            intent = new Intent(this, AddSlideActivity.class);
        } else if (id == R.id.action_remove) {
            intent = new Intent(this, RemoveSlideActivity.class);
        } else if (id == R.id.action_return) {
            intent = new Intent(this, SlideShowActivity.class);
        }
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
