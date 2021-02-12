import React from 'react';
import { render } from '@testing-library/react';
import App from './App';

test('Renders the start recording button', () => {
  const { getByText } = render(<App />);
  const startButton = getByText(/Start Recording/);
  expect(startButton).toBeInTheDocument();
});

test('Renders the stop recording button', () => {
  const { getByText } = render(<App />);
  const stopButton = getByText(/Stop Recording/);
  expect(stopButton).toBeInTheDocument();
});
