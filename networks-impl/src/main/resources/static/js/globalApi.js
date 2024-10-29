const endpointPrefix = 'http://127.0.0.1:23567/api/v1/graph';

const onGenerateNodesClick = () => {
  $('#generateNodesButton').click(() => {
    const amount = prompt("Enter the amount of nodes to generate:");

    if (amount > 0) {
      startLoad();

      $
      .post(
          `${endpointPrefix}/generate?amount=${amount}`,
          () => onSuccess('Nodes — generated')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Invalid amount. Must be greater than 0');
    }
  });
}

const onGenerateLeftSplitNodesClick = () => {
  $('#generateLeftSplitNodesButton').click(() => {
    const amount = prompt(
        "Enter the amount of nodes to generate for the left split:");

    if (amount > 0) {
      startLoad();

      $
      .post(
          `${endpointPrefix}/generateLeftSplit?amount=${amount}`,
          () => onSuccess('Left split nodes — generated')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Invalid amount. Must be greater than 0');
    }
  });
}

const onGenerateRightSplitNodesClick = () => {
  $('#generateRightSplitNodesButton').click(() => {
    const amount = prompt(
        "Enter the amount of nodes to generate for the right split:");

    if (amount > 0) {
      startLoad();

      $
      .post(
          `${endpointPrefix}/generateRightSplit?amount=${amount}`,
          () => onSuccess('Right split nodes — generated')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Invalid amount. Must be greater than 0');
    }
  });
}

const onDisplayGraphClick = () => {
  $('#displayGraphButton').click(() => {
    startLoad();

    $
    .get(`${endpointPrefix}/display`, (data) => {
      toastr.success('Graph — displayed');

      console.log(data);

      const cy = cytoscape({
        container: document.getElementById('cy'),
        elements: data.cytoscape,
        style: [
          {
            selector: 'node',
            style: {
              'background-color': '#666',
              'label': 'data(id)'
            }
          },
          {
            selector: 'edge',
            style: {
              'width': 3,
              'line-color': '#ccc',
              'target-arrow-color': '#ccc',
              'target-arrow-shape': 'triangle'
            }
          }
        ],
        layout: {
          name: 'cose',
          nodeRepulsion: 10000,
          idealEdgeLength: 100,
          edgeElasticity: 100,
          nestingFactor: 0.1,
        },
        maxZoom: 3,
        minZoom: 0.5,
        styleEnabled: true,
        wheelSensitive: 0.1
      });

      $('#cy-parent').addClass('bg-light');

      $('#cy').css({
        'width': '100%',
        'height': '500px'
      });

      cy.resize();

      cy.fit();
    })
    .fail(onFail)
    .always(endLoad);
  });
}

const onDeleteGraphClick = () => {
  $('#deleteButton').click(() => {
    startLoad();

    $
    .post(
        `${endpointPrefix}/delete`,
        () => onSuccess('Graph — deleted')
    )
    .fail(onFail)
    .always(endLoad);
  });
}