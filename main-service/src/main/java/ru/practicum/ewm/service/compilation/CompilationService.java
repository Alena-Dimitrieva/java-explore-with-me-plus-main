package ru.practicum.ewm.service.compilation;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CompilationSearchFilter;
import ru.practicum.ewm.dto.compilation.CompilationUpdateDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;

import java.util.List;

public interface CompilationService {

	CompilationDto getById(Long compilationId, HttpServletRequest request);

	void delById(Long compilationId);

	CompilationDto addCompilation(NewCompilationDto compilation);

	CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilation);

	List<CompilationDto> getByFilter(CompilationSearchFilter filter, HttpServletRequest request);
}
