package com.ProjetoSD_META2.ProjetoSD_META2.Spring;

import com.ProjetoSD_META2.ProjetoSD_META2.Component;
import com.ProjetoSD_META2.ProjetoSD_META2.Searched;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record StatsMessage(HashMap<String, Component> components, List<Searched> topSearches) {
}
