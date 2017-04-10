package io.ipoli.android.app.ui.viewmodels;

import io.ipoli.android.quest.data.Category;

public class AndroidCalendarViewModel {
    private Long id;
    private String name;
    private Category category;
    private boolean isSelected;

    public AndroidCalendarViewModel(Long id, String name, Category category, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.isSelected = isSelected;
    }

    public void select() {
        isSelected = true;
    }

    public void deselect() {
        isSelected = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isSelected() {
        return isSelected;
    }
}