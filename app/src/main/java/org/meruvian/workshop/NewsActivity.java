package org.meruvian.workshop;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.meruvian.workshop.adapter.NewsAdapter;
import org.meruvian.workshop.content.database.adapter.NewsDatabaseAdapter;
import org.meruvian.workshop.entity.News;
import org.meruvian.workshop.kategori.activity_kategori;
import org.meruvian.workshop.rest.RestVariables;
import org.meruvian.workshop.service.TaskService;
import org.meruvian.workshop.task.NewsDeleteTask;
import org.meruvian.workshop.task.NewsGetTask;
import org.meruvian.workshop.task.NewsPostTask;
import org.meruvian.workshop.task.NewsPutTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ludviantoovandi on 25/02/15.
 */
public class NewsActivity extends ActionBarActivity implements TaskService {
    NewsDatabaseAdapter newsDatabaseAdapter;
    EditText content, title;
    NewsPostTask newsPostTask;
    NewsPutTask newsPutTask;
    Button kategoriId;
    private ListView listNews;
    private NewsAdapter newsAdapter;
    private News news;
    private ProgressDialog progressDialog;
    private NewsGetTask newsGetTask;
    private NewsDeleteTask newsDeleteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        listNews = (ListView) findViewById(R.id.list_news);
        content = (EditText) findViewById(R.id.edit_content);
        title = (EditText) findViewById(R.id.edit_title);
        kategoriId = (Button) findViewById(R.id.kategori);

        newsDatabaseAdapter = new NewsDatabaseAdapter(this);
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());
        listNews.setAdapter(newsAdapter);

        listNews.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogList(i);

                return true;
            }
        });

        kategoriId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NewsActivity.this, activity_kategori.class));
            }
        });

        newsGetTask = new NewsGetTask(this, this);
        newsGetTask.execute("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                newsGetTask = new NewsGetTask(NewsActivity.this, NewsActivity.this);
                newsGetTask.execute(s);

                return false;
            }
            @Override
            public boolean onQueryTextChange(String s)
            {
                return false;
            }
        });
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (news == null) {
                news = new News();
            }
            news.setStatus(1);
            news.setContent(content.getText().toString());
            news.setTitle(title.getText().toString());
            news.setCreateDate(new Date().getTime());
            if (news.getId() == -1) {
                newsPostTask = new NewsPostTask(this, this);
                newsPostTask.execute(news);
            } else {
                newsPutTask = new NewsPutTask(this, this);
                newsPutTask.execute(news);
            }
            news = new News();
        } else if (item.getItemId() == R.id.action_refresh) {
            newsGetTask = new NewsGetTask(this, this);
            newsGetTask.execute("");
        }
        return true;
    }

    private void dialogList(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.action));
        builder.setItems(new String[] {getString(R.string.edit), getString(R.string.delete)}, new
                DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int location) {
                        news = (News) newsAdapter.getItem(position);
                        if (news != null) {
                            if (location == 0) {
                                title.setText(news.getTitle());
                                content.setText(news.getContent());
                                title.requestFocus();
                            } else if (location == 1) {
                                confirmDelete();
                            }
                        }
                    }
                });
        builder.create().show();

    }

    private void confirmDelete()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete));
        builder.setMessage(getString(R.string.confirm_delete) + " '"+ news.getTitle() + "' ?");
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                newsDeleteTask = new NewsDeleteTask(NewsActivity.this, NewsActivity.this);
                newsDeleteTask.execute(news.getId() + "");
                news = new News();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }


    @Override
    public void onExecute(int code)
    {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (newsGetTask != null) {
                            newsGetTask.cancel(true);
                        }
                    }
                });
                progressDialog.show();
            }
        } else {
            progressDialog = new ProgressDialog(this);
        }

    }

    @Override
    public void onSuccess(int code, Object result)
    {
        if (result != null) {
            if (code == RestVariables.NEWS_GET_TASK) {
                List<News> newses = (List<News>) result;
                newsAdapter.clear();
                newsAdapter.addNews(newses);
            } else if (code == RestVariables.NEWS_POST_TASK) {
                title.setText("");
                content.setText("");
                news = new News();
                News news = (News) result;
                newsAdapter.addNews(news);
            } else if (code == RestVariables.NEWS_PUT_TASK) {
                title.setText("");
                content.setText("");
                news = new News();
                newsGetTask = new NewsGetTask(this, this);
                newsGetTask.execute("");
            } else if (code == RestVariables.NEWS_DELETE_TASK) {
                newsGetTask = new NewsGetTask(this, this);
                newsGetTask.execute("");
            }
        }
        progressDialog.dismiss();
    }

    @Override
    public void onCancel(int code, String message)
    {
        progressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(int code, String message)
    {
        progressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}